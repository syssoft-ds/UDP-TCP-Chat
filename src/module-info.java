module Client {
    requires javafx.controls;
    requires javafx.graphics;

    opens Client;
    exports Client to javafx.graphics;
}