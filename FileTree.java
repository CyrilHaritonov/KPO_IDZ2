import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileTree {
    // дерево файлов, в котором узлы упорядочены по высоте по порядку,
    // в котором они зависят друг от друга (через require), так вершины,
    // которые не зависят от чужих require стоят на высоте 1 (являются детьми root)
    static class Node {
        // узел дерева файлов, который хранит информацию о файле, который мы обрабатываем
        private final String value; // путь до файла
        private final List<Node> children; // файлы, которые данный файл вызывает через require

        private String text = ""; // текстовое содержимое данного файла

        String getText() {
            return text;
        }

        public List<Node> getChildren() {
            return children;
        }

        public String getValue() {
            return value;
        }

        void setText(String line) {
            text = line;
        }

        Node(String value) {
            this.value = value;
            children = new ArrayList<>();
        }
    }

    final Node root;

    /**
     * обходим дерево файлов в ширину, чтобы вывести сначала узлы, которые не от кого не зависят, а потом зависимые вершины
     * @param current текущая вершина, изначально root дерева файлов
     * @return возвращает обход дерева файлов в ширину в виде массива формата ArrayList<Node>
     */

    List<Node> traversal(Node current) {
        List<Node> partOfTraversal = new ArrayList<>();
        partOfTraversal.add(current);
        for (Node child : current.children) {
            partOfTraversal.addAll(traversal(child));
        }
        return partOfTraversal;
    }

    /**
     * проверяем содержится ли данный файл в дереве файлов
     * @param key путь до файла
     * @return true если содержится, иначе false
     */
    boolean contains(String key) {
        return traversal(root).stream().anyMatch(node -> Objects.equals(key, node.value));
    }

    /**
     * поиск файла в дереве файлов
     * @param key путь до файла
     * @return если файл найден, то возращает Node с информацией о данном файле, иначе null
     */
    Node find(String key) {
        List<Node> candidates = traversal(root).stream().filter(node -> Objects.equals(node.value, key)).toList();
        if (!candidates.isEmpty()) {
            return candidates.get(0);
        } else {
            return null;
        }
    }

    /**
     * ищет файл, который вызывает текущий файл через require
     * @param current Node с информацией о текущем файле
     * @return если родитель найден, то возращает Node с информацией о родителе, иначе null
     */
    Node findParent(Node current) {
        return traversal(root).stream().map(node -> {
            Node parent = null;
            if (!node.children.stream().filter(child -> Objects.equals(child.value, current.value)).toList().isEmpty()) {
                parent = node;
            }
            return parent;
        }).filter(Objects::nonNull).toList().get(0);
    }

    /**
     * добавляем в дерево файлов новый узел, с информацией о текущем файле
     * @param parent Node с информацией о файле, который вызывает текущий файл через require, либо root дерева файлов
     * @param key путь до текущего файла
     * @return Node с информацией о текущем файле
     */
    Node insert(Node parent, String key) {
        Node nodeThatAlreadyExists = find(key); // если файл уже в дереве файлов, то находим его
        if (nodeThatAlreadyExists != null) {
            Node parentCopy = findParent(nodeThatAlreadyExists); // ищем родителя в дереве файлов
            parentCopy.children.remove(nodeThatAlreadyExists); // убираем данный файл из детей родителя который уже в дереве
            parent.children.add(nodeThatAlreadyExists); // добавляем данный файл в дети к родителю, который передан в аргументе
            if (find(nodeThatAlreadyExists.value) == null) { // если после данной манипуляции узел полностью пропал из дерева файлов, то значит была циклическая зависимость
                System.out.println("Обнаруженная циклическая зависимость!");
                System.out.println("Проблема с файлами: " + nodeThatAlreadyExists.getValue() + " и " + parent.getValue());
                System.exit(0);
            }
            return nodeThatAlreadyExists;
        } else {
            Node newNode = new Node(key); // если данного файла нет в дереве файлов, то просто создаем новый узел
            parent.children.add(newNode); // и добавляем ребенка родителю указанному в аргументе
            return newNode;
        }
    }

    FileTree(String value) {
        root = new Node(value);
    }
}
