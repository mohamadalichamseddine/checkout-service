package com.checkoutservice.app;

import com.checkoutservice.app.beans.*;

import com.checkoutservice.domain.cart.Cart;
import com.checkoutservice.domain.cart.CartItem;
import com.checkoutservice.domain.cart.CartRepository;
import com.checkoutservice.domain.cart.Money;

import com.checkoutservice.domain.order.Order;
import com.checkoutservice.domain.order.OrderRepository;

import com.checkoutservice.domain.payment.FailPayStrategy;
import com.checkoutservice.domain.payment.MockPayStrategy;
import com.checkoutservice.domain.payment.CashOnDeliveryStrategy;
import com.checkoutservice.domain.payment.PaymentStrategy;

import com.checkoutservice.domain.pricing.BasePriceCalculator;
import com.checkoutservice.domain.pricing.CouponCalculator;
import com.checkoutservice.domain.pricing.PricingCalculator;
import com.checkoutservice.domain.pricing.PricingContext;
import com.checkoutservice.domain.pricing.TaxCalculator;
import com.checkoutservice.domain.pricing.ShippingCalculator;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutService {

    private final CartRepository carts;
    private final OrderRepository orders;

    /**
     * Pricing pipeline (Chain of Responsibility):
     * Base → Coupon → Tax(15%) → Shipping($5, free ≥ $50)
     * All amounts are doubles (no cents type).
     */
    private final PricingCalculator calculator =
            new BasePriceCalculator()
                    .setNext(new CouponCalculator())
                    .setNext(new TaxCalculator(0.15))            // 15% tax (demo)
                    .setNext(new ShippingCalculator(5.0, 50.0)); // $5 shipping; free over $50

    public CheckoutService(CartRepository carts, OrderRepository orders) {
        this.carts = carts;
        this.orders = orders;
    }

    // ===== Carts =====

    public CreateCartResult createCart(CreateCartJob createCartJob) {
        var createdCartId = carts.create(createCartJob.currency());
        return new CreateCartResult(createdCartId);
    }

    private List<CartItemResult> buildCartItemResults(List<CartItem> domainCartItems){
        var items = new ArrayList<CartItemResult>();
        for(var cartItem : domainCartItems){
            items.add(new CartItemResult(cartItem.productId(), cartItem.qty(), cartItem.unitPrice().amount(), cartItem.lineTotal().amount()));
        }

        return items;
    }

    public GetCartResult addItemToCart(String id, AddCartItemJob addCartItemJob) {
        // Use the cart's currency when constructing Money for the item
        Cart cart = carts.get(id);
        CartItem item = new CartItem(addCartItemJob.productId(), addCartItemJob.qty(), new Money(addCartItemJob.unitPrice(), cart.currency()));
        Cart cartResult = carts.addItem(id, item);

        return new GetCartResult(cartResult.id(), cartResult.currency(), buildCartItemResults(cartResult.items()));
    }

    public GetCartResult getCart(String id) {
        Cart cart = carts.get(id);
        return new GetCartResult(cart.id(), cart.currency(), buildCartItemResults(cart.items()));
    }

    // ===== Checkout =====

    public CheckoutResult start(CheckoutJob checkoutJob) {
        Cart cart = carts.get(checkoutJob.cartId());

        // Run pricing pipeline
        PricingContext pricingContext = calculator.calculate(new PricingContext(cart, checkoutJob.couponCode()));

        // Determine currency (prefer request; fallback to cart)
        String currency = (checkoutJob.currency() == null || checkoutJob.currency().isBlank())
                ? cart.currency()
                : checkoutJob.currency();

        // Create Order with total as Money(double, currency)
        Order order = new Order(new Money(pricingContext.total, currency));

        // Pick payment strategy at runtime
        String providerName = (checkoutJob.paymentProvider() == null || checkoutJob.paymentProvider().isBlank())
                ? "MockPay"
                : checkoutJob.paymentProvider();

        PaymentStrategy strategy = switch (providerName) {
            case "FailPay" -> new FailPayStrategy();
            case "COD", "CashOnDelivery" -> new CashOnDeliveryStrategy();
            default -> new MockPayStrategy();
        };

        boolean ok = strategy.pay(pricingContext.total);
        if (ok) {
            order.onPaymentSucceeded();
        } else {
            order.onPaymentFailed();
        }

        orders.save(order);
        return new CheckoutResult(order.getId(), order.stateName(), pricingContext.total, strategy.name());
    }

    // ===== Orders =====

    public GetOrderResult getOrder(String id) {
        var order = orders.get(id);
        return new GetOrderResult(order.getId(), order.amount().amount(), order.amount().currency(), order.stateName());
    }
}
