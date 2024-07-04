package com.GujjuSajang.cart.service;

import com.GujjuSajang.cart.dto.UpdateCartProductDto;
import com.GujjuSajang.cart.repository.CartRedisRepository;
import com.GujjuSajang.core.dto.CartDto;
import com.GujjuSajang.core.dto.CartProductsDto;
import com.GujjuSajang.core.dto.CreateMemberEventDto;
import com.GujjuSajang.core.exception.CartException;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.service.EventProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRedisRepository cartRedisRepository;
    private final EventProducerService eventProducerService;

    @Transactional
    @KafkaListener(topics = {"create-member"}, groupId = "createCart")
    public void createCart(CreateMemberEventDto createMemberEventDto) {
        try {
            cartRedisRepository.save(createMemberEventDto.getId(), new CartDto());
        } catch (Exception e) {
            eventProducerService.sendEvent("fail-create-cart", createMemberEventDto);
        }
    }

    public CartDto addCartProduct(Long memberId, CartProductsDto cartProductsDto) {
        CartDto cartDto = getCart(memberId);
        cartDto.getCartProductsDtos().add(cartProductsDto);
        cartRedisRepository.save(memberId, cartDto);
        return cartDto;
    }

    public CartDto getCart(Long memberId) {
        return cartRedisRepository.get(memberId).orElseThrow(() -> new CartException(ErrorCode.CART_NOT_FOUND));
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
