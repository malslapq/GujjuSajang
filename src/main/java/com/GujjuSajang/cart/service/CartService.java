package com.GujjuSajang.cart.service;

import com.GujjuSajang.cart.dto.CartDto;
import com.GujjuSajang.cart.dto.CartProductsDto;
import com.GujjuSajang.cart.dto.UpdateCartProductDto;
import com.GujjuSajang.cart.repository.CartRepository;
import com.GujjuSajang.exception.CartException;
import com.GujjuSajang.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRedisRepository;

    public CartDto addCartProduct(Long id, CartProductsDto cartProductsDto) {
        CartDto cartDto = cartRedisRepository.get(id).orElse(new CartDto());
        cartDto.getCartProductsDtos().add(cartProductsDto);
        cartRedisRepository.save(id, cartDto);
        return cartDto;
    }

    public CartDto updateCart(Long id, UpdateCartProductDto updateCartProductDto) {

        CartDto getCart = cartRedisRepository.get(id).orElseThrow(() -> new CartException(ErrorCode.INVALID_CART_UPDATE));

        getCart.getCartProductsDtos().stream()
                .filter(cartProductsDto -> cartProductsDto.getProductID().equals(updateCartProductDto.getProductId()))
                .findFirst()
                .ifPresent(cartProductsDto -> cartProductsDto.setCount(updateCartProductDto.getCount()));

        cartRedisRepository.save(id, getCart);
        return getCart;
    }

    public CartDto getCart(Long id, Long tokenId) {
        validateMemberId(id, tokenId);
        return cartRedisRepository.get(id).orElse(new CartDto());
    }

    public void deleteCart(Long id, Long tokenId) {
        validateMemberId(id, tokenId);
        cartRedisRepository.delete(id);
    }

    private static void validateMemberId(Long id, Long tokenId) {
        if (!id.equals(tokenId)) {
            throw new CartException(ErrorCode.MISS_MATCH_MEMBER);
        }
    }

}
