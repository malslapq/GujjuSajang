package com.GujjuSajang.cart.service;

import com.GujjuSajang.cart.dto.UpdateCartProductDto;
import com.GujjuSajang.cart.repository.CartRedisRepository;
import com.GujjuSajang.core.dto.CartDto;
import com.GujjuSajang.core.dto.CartProductsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRedisRepository cartRedisRepository;

    public CartDto addCartProduct(Long memberId, CartProductsDto cartProductsDto) {
        CartDto cartDto = getCart(memberId);
        cartDto.getCartProductsDtos().add(cartProductsDto);
        cartRedisRepository.save(memberId, cartDto);
        return cartDto;
    }

    public CartDto getCart(Long memberId) {
        return cartRedisRepository.get(memberId).orElse(new CartDto());
    }

    public CartDto updateCart(Long memberId, Long productId, UpdateCartProductDto updateCartProductDto) {

        CartDto getCart = getCart(memberId);

        getCart.getCartProductsDtos().stream()
                .filter(cartProductsDto -> cartProductsDto.getProductId().equals(productId))
                .findFirst()
                .ifPresent(cartProductsDto -> cartProductsDto.setCount(updateCartProductDto.getCount()));

        cartRedisRepository.save(memberId, getCart);
        return getCart;
    }

    public CartDto deleteCartProduct(Long memberId, Long productId) {
        CartDto cart = getCart(memberId);
        cart.getCartProductsDtos().removeIf(cartProductsDto -> cartProductsDto.getProductId().equals(productId));
        cartRedisRepository.save(memberId, cart);
        return cart;
    }

    public void deleteCart(Long memberId) {
        cartRedisRepository.save(memberId, new CartDto());
    }

}
