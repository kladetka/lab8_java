package hotel.cli;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Тонкий консольний клієнт: показує меню, читає вибір, викликає команду. Нічого не рахує.
public class ConsoleUi {
    private final ConsoleIO io;
    private final Map<String, ConsoleCommand> commands = new LinkedHashMap<>();

    public ConsoleUi(ConsoleIO io, List<ConsoleCommand> commandList) {
        this.io = io;
        for (ConsoleCommand c : commandList) {
            commands.put(c.key(), c);
        }
    }

    public void run() {
        io.println("=== Бронювання номера в готелі ===");
        boolean running = true;
        while (running) {
            for (ConsoleCommand c : commands.values()) {
                io.println(c.key() + " - " + c.title());
            }
            String choice = io.ask("Ваш вибір: ");
            ConsoleCommand command = commands.get(choice);
            if (command == null) {
                io.println("Невідомий пункт меню: " + choice);
            } else {
                running = command.execute();
            }
        }
    }
}
