import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Iterators {
    /**
     * просматриваем текст файла и require внутри него, записываем результат в дерево файлов
     *
     * @param file           файл, который надо обработать
     * @param fileTree       дерево файлов, в которое будет вставлен результат
     * @param parent         файл, который обрабатывали ранее, в случае первого запуска это root дерева файлов, иначе файл, который обрабатывался методом, вызвавшим данный метод
     * @param rootFolderPath путь до корневой папки
     * @return возвращает текст файла, в котором require заменены на соответствующий им текст
     */
    static String iterateOverRequires(File file, FileTree fileTree, FileTree.Node parent, String rootFolderPath) {
        try {
            StringBuilder temporaryText = new StringBuilder(); // будем сохранять сюда текст, который спарсили из файла и текст, полученный вместо require
            FileTree.Node prev = fileTree.insert(parent, file.getPath()); // добавляем новый узел в дерево файлов
            if (file.isDirectory()) { // текст подкатолога считать не можем, поэтому выходим
                return "";
            }
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String currentLine = scanner.nextLine();
                    if (!currentLine.contains("require")) {
                        temporaryText.append(currentLine).append("\n");
                        continue;
                    }
                    String filePath = currentLine.split(" ")[1]; // нашли строку вида "require 'filename'", берем из нее 'filename'
                    filePath = filePath.substring(1, filePath.length() - 1); // избавляемся от кавычек
                    try {
                        temporaryText.append(iterateOverRequires(new File(rootFolderPath + '/' + filePath), fileTree, prev, rootFolderPath)); // обрабатываем файл, на который ссылается require
                    } catch (NullPointerException error) {
                        System.out.println("Неверноое имя файла" + filePath);
                        System.exit(1);
                    }
                }
                prev.setText(temporaryText.toString()); // записываем текст, который считали из файла в соответствующий ему узел дерева файлов
                return temporaryText.toString();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Не найден файл: " + file);
            System.exit(1);
        }
        return "";
    }

    /**
     * обходим все файлы и подкатологи текущего католога и обрабатываем их содержимое
     *
     * @param currentFolder  текущая папка, изначально root дерева файлов
     * @param fileTree       дерево файлов, в которое будем записываеться результат
     * @param rootFolderPath путь до корневой папки (root дерева файлов)
     */
    static void iterateOverFiles(File currentFolder, FileTree fileTree, String rootFolderPath) {
        File[] files = currentFolder.listFiles(); // получаем все файлы текущего каталога
        if (files == null) { // если их нет то выходим
            return;
        }
        for (File file : files) { // обходим все файлы текущего каталога
            if (fileTree.contains(file.getPath())) { // если файл уже в дереве файлов, то не обрабатываем его
                continue;
            }
            iterateOverFiles(file, fileTree, rootFolderPath); // вызываем обработку содержимого данного файла (в случае если это подкаталог)
            iterateOverRequires(file, fileTree, fileTree.root, rootFolderPath); // обрабатываем текст данного файла
        }

    }
}
