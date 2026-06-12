package com.example.consolemvc.view;

import com.example.consolemvc.model.Order;

import java.util.List;
import java.util.Scanner;

public class OrderView {

    private final Scanner scanner = new Scanner(System.in);

    public void printMenu() {
        System.out.println("\n========== 주문 관리 시스템 ==========");
        System.out.println("1. 주문 생성");
        System.out.println("2. 전체 주문 조회");
        System.out.println("3. 주문 상세 조회");
        System.out.println("4. 주문 상태 변경");
        System.out.println("5. 주문 취소");
        System.out.println("0. 종료");
        System.out.print("> ");
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public void printMessage(String message) {
        System.out.println(message);
    }

    public void printOrder(Order order) {
        System.out.println("------------------------------------------");
        System.out.printf("ID         : %d%n", order.getId());
        System.out.printf("주문자     : %s%n", order.getCustomerName());
        System.out.printf("상품명     : %s%n", order.getProductName());
        System.out.printf("수량       : %d%n", order.getQuantity());
        System.out.printf("상태       : %s%n", order.getStatus());
        System.out.printf("주문일시   : %s%n", order.getCreatedAt());
        System.out.println("------------------------------------------");
    }

    public void printOrderList(List<Order> orders) {
        if (orders.isEmpty()) {
            System.out.println("등록된 주문이 없습니다.");
            return;
        }
        System.out.println("------------------------------------------");
        System.out.printf("%-5s %-10s %-12s %-6s %-10s%n",
                "ID", "주문자", "상품명", "수량", "상태");
        System.out.println("------------------------------------------");
        for (Order o : orders) {
            System.out.printf("%-5d %-10s %-12s %-6d %-10s%n",
                    o.getId(), o.getCustomerName(), o.getProductName(),
                    o.getQuantity(), o.getStatus());
        }
        System.out.println("------------------------------------------");
    }
}
