package com.GujjuSajang.orders.service;

import com.GujjuSajang.orders.repository.OrdersProductRepository;
import com.GujjuSajang.orders.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final OrdersProductRepository ordersProductRepository;
//    private final CartRedisRepository  cartRedisRepository;
//    private final StockRepository stockRepository;
//    private final ProductRepository productRepository;
//    private final StockService stockService;

//    // 주문 생성
//    @Transactional
//    public OrdersDto createOrder(Long memberId, Long tokenId) {
//
//        validateMemberId(memberId, tokenId);
//
//        // 장바구니 가져오기
//        CartDto cartDto = cartRedisRepository.get(memberId).orElseThrow(() -> new OrdersException(ErrorCode.NOT_FOUND_ORDER_PRODUCT));
//
//        // 제품 아이디 값 가져오기
//        List<Long> productIds = cartDto.getCartProductsDtos().stream().map(CartProductsDto::getProductID).toList();
//
//        // 재고 가져와서 제품 아이디를 key 재고수를 value 로 HashMap 생성
//        List<Stock> stocks = stockRepository.findAllByProductIdIn(productIds);
//        Map<Long, Integer> stockCountMap = stocks.stream().collect(Collectors.toMap(Stock::getProductId, Stock::getCount));
//
//        // 재고가 더 적을 경우 예외 처리
//        for (CartProductsDto cartProductsDto : cartDto.getCartProductsDtos()) {
//            int orderProductCount = cartProductsDto.getCount();
//            int stockCount = stockCountMap.get(cartProductsDto.getProductID());
//            if (orderProductCount > stockCount) {
//                throw new OrdersException(ErrorCode.NOT_ENOUGH_STOCK);
//            }
//            stockCountMap.put(cartProductsDto.getProductID(), stockCount - orderProductCount);
//        }
//
//        // 재고 업데이트
//        for (Stock stock : stocks) {
//            int curStockCount = stockCountMap.get(stock.getProductId());
//            stock.changeCount(curStockCount);
//        }
//
//        //주문 생성
//        Orders orders = ordersRepository.save(Orders.from(memberId, cartDto));
//
//        // 주문상세 제품 생성
//        List<OrdersProduct> ordersProducts = cartDto.getCartProductsDtos().stream()
//                .map(cartProductsDto -> OrdersProduct.from(orders.getId(), cartProductsDto)).toList();
//
//        ordersProductRepository.saveAll(ordersProducts);
//
//        cartRedisRepository.delete(memberId);
//
//        return OrdersDto.from(orders);
//    }
//
//    // 주문 조회
//    @Transactional(readOnly = true)
//    public OrdersPageDto getOrder(Long memberId, Long tokenMemberId, Pageable pageable) {
//        validateMemberId(memberId, tokenMemberId);
//        Page<Orders> orders = ordersRepository.findByMemberIdOrderByCreateAtDesc(memberId, pageable);
//        List<OrdersDto> ordersDtos = orders.getContent().stream().map(OrdersDto::from).toList();
//        return OrdersPageDto.builder()
//                .ordersDtos(ordersDtos)
//                .pageNumber(orders.getNumber())
//                .pageSize(orders.getSize())
//                .totalCount(orders.getTotalElements())
//                .totalPage(orders.getTotalPages())
//                .last(orders.isLast())
//                .build();
//    }
//
//    // 주문 내역 상세 조회
//    @Transactional
//    public List<OrdersProductDto> getOrderProducts(Long orderId) {
//        List<OrdersProduct> ordersProducts = ordersProductRepository.findByOrdersId(orderId);
//
//        if (ordersProducts.isEmpty()) {
//            throw new OrdersException(ErrorCode.NOT_FOUND_ORDER_PRODUCT);
//        }
//
//        // 상태 업데이트
//        ordersProducts.forEach(ordersProduct -> {
//
//            // 주문이 반품완료 된 건이라면 재고 업데이트
//            if (ordersProduct.changeDeliveryStatus()) {
//                stockService.updateStock(StockDto.builder()
//                        .productId(ordersProduct.getProductId())
//                        .count(ordersProduct.getCount())
//                        .build());
//            }
//        });
//
//        List<Long> productIds = ordersProducts.stream().map(OrdersProduct::getProductId).toList();
//        Map<Long, String> productNameMap = productRepository.findAllById(productIds).stream()
//                .collect(Collectors.toMap(Product::getId, Product::getName));
//
//        return ordersProducts.stream()
//                .map(orderProduct -> OrdersProductDto.from(orderProduct, productNameMap))
//                .toList();
//    }
//
//    // 주문 제품 취소
//    @Transactional
//    public OrdersProductDto cancelOrderProduct(Long memberId, Long orderProductId, Long tokenMemberId) {
//
//        validateMemberId(memberId, tokenMemberId);
//        OrdersProduct ordersProduct = getOrderProduct(orderProductId);
//        validateOrderProductStatus(ordersProduct.getStatus(), OrdersStatus.COMPLETE);
//        ordersProduct.changeStatus(OrdersStatus.CANCEL);
//        Product product = productRepository.findById(ordersProduct.getProductId()).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_PRODUCT));
//        validateOrderPeriods(ordersProduct.getCreateAt().plusDays(1));
//
//        Stock stock = stockRepository.findByProductId(ordersProduct.getProductId()).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_STOCK));
//        stock.updateCount(ordersProduct.getCount());
//
//        return OrdersProductDto.from(ordersProduct, product);
//    }
//
//    // 주문 제품 반품 신청
//    @Transactional
//    public OrdersProductDto returnOrderProduct(Long memberId, Long orderProductId, Long tokenMemberId) {
//        validateMemberId(memberId, tokenMemberId);
//        OrdersProduct ordersProduct = getOrderProduct(orderProductId);
//        validateOrderProductStatus(ordersProduct.getStatus(), OrdersStatus.COMPLETED_DELIVERY);
//        Product product = productRepository.findById(ordersProduct.getProductId()).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_PRODUCT));
//        validateOrderPeriods(ordersProduct.getUpdateAt().plusDays(1));
//        ordersProduct.changeStatus(OrdersStatus.RETURN_REQUEST);
//
//        return OrdersProductDto.from(ordersProduct, product);
//    }
//
//    private static void validateOrderPeriods(LocalDateTime orderCreateAtPlusDays) {
//        if (LocalDateTime.now().isAfter(orderCreateAtPlusDays)) {
//            throw new OrdersException(ErrorCode.ORDER_CANCELLATION_PERIOD_EXPIRED);
//        }
//    }
//
//    private static void validateOrderProductStatus(OrdersStatus orderProductStatus, OrdersStatus status) {
//        if (orderProductStatus != status) {
//            throw new OrdersException(ErrorCode.ACTION_NOT_ALLOWED);
//        }
//    }
//
//    private OrdersProduct getOrderProduct(Long orderProductId) {
//        return ordersProductRepository.findById(orderProductId).orElseThrow(() -> new OrdersException(ErrorCode.NOT_FOUND_ORDERED_PRODUCT));
//    }
}
