import java.io.File;

public class Main {
    public static void main(String[] args) {
        try {
            File rootFolder = new File(args[0]); // открываем указанный каталог
            FileTree fileTree = new FileTree(rootFolder.getName()); // создаем дерево файлов для него
            Iterators.iterateOverFiles(rootFolder, fileTree, rootFolder.getPath()); // обходим все файлы внутри данного каталога и его подкаталогов
            fileTree.traversal(fileTree.root).forEach(file -> { // выводим их в порядке зависимости: снизу зависимые, выше те от которых зависят
                if (!new File(file.getValue()).isDirectory()) { // подкатологи пропускаем
                    System.out.println(file.getValue());
                }
            });
            fileTree.root.getChildren().forEach(child -> { // выводим текст не от чего независящих файлов с уже замененными вставками require
                if (child.getText().length() > 0) { // пропускаем подкатологи либо просто пустые файлы
                    System.out.println(child.getText().substring(0, child.getText().length() - 1));
                }
            });
        } catch (NullPointerException error) {
            System.out.println("Неверный путь до корневой папки!");
            System.exit(1);
        }
    }
}