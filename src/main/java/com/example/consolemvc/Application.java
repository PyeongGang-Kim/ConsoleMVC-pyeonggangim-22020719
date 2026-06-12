package com.example.consolemvc;

import com.example.consolemvc.controller.OrderController;
import com.example.consolemvc.repository.InMemoryOrderRepository;
import com.example.consolemvc.repository.OrderRepository;
import com.example.consolemvc.service.OrderService;
import com.example.consolemvc.view.OrderView;

import java.util.Scanner;

public class Application {
    public static void main(String[] args) {
        OrderRepository repository = new InMemoryOrderRepository();
        OrderService service = new OrderService(repository);
        OrderView view = new OrderView();
        OrderController controller = new OrderController(service, view);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            view.printMenu();
            String input = scanner.nextLine().trim();
            if ("0".equals(input)) {
                view.printMessage("시스템을 종료합니다.");
                break;
            }
            controller.handleMenu(input);
        }
    }
}
