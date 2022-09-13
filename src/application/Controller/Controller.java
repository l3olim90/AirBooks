package application.Controller;

import application.Main;
import application.Model.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Controller{
    @FXML
    private Hyperlink aboutProgLink;
    @FXML
    private TextField usernameTxtField;
    @FXML
    private PasswordField passwordTxtField;
    @FXML
    private Button loginBtn;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label walletAmtLabel;
    @FXML
    private Label itemsCartLabel;
    @FXML
    private VBox lockerHBox;
    @FXML
    private TextField postalTxtField;
    @FXML
    private TextField lockerNumTxtField;
    @FXML
    private TextField lockerPassTxtField;
    @FXML
    private Button unlockLockerBtn;
    @FXML
    private VBox postLoginVBox;
    @FXML
    private Label subjCodeISBNLabel;
    @FXML
    private TextField subjCodeISBN;
    @FXML
    private Button optionOneBtn;
    @FXML
    private Button optionTwoBtn;
    @FXML
    private Button optionThreeBtn;
    @FXML
    private TextArea textArea;
    @FXML
    private HBox itemChoiceHBox;
    @FXML
    private TextField itemSelect;
    @FXML
    private Button addRemoveItemBtn;
    @FXML
    private HBox postalCodeHBox;
    @FXML
    private Button checkoutBtn1;
    @FXML
    private TextField postalText;

    private Database db;
    private Security secure;
    private ArrayList<Book> rentalCart;
    private ArrayList<Book> viewBooks;
    private ArrayList<SelfCollectStn> sCS;
    private boolean adminLogin;
    private boolean currLogin;
    private boolean addFlag;
    private boolean checkOutFlag;

    public Controller(){
        db = new Database("Student.csv", "Books.csv", "SelfCollectStn.csv", "DistrictAreas.csv");
        secure = new Security("Secure.csv");
        adminLogin = false;
        currLogin = false;
        rentalCart = new ArrayList<Book>();
        viewBooks = new ArrayList<Book>();
        sCS = new ArrayList<SelfCollectStn>();
        addFlag = false;
        checkOutFlag = false;
    }

    public void aboutProg(ActionEvent actionEvent) {
        Stage popupwindow = new Stage();
        popupwindow.setTitle("About Programmer");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/application/View/aboutProg.fxml"));
            popupwindow.setScene(new Scene(root));
            popupwindow.initModality(Modality.APPLICATION_MODAL);
            popupwindow.initOwner(aboutProgLink.getScene().getWindow());
            popupwindow.showAndWait();
        }catch(Exception e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("About Programmer cannot be loaded!");
            alert.showAndWait();
        }
    }

    public void login(ActionEvent actionEvent) {
        if (currLogin){ //If there is a current login
            loginBtn.setText("Login");
            usernameTxtField.setDisable(false); usernameTxtField.setText("");
            passwordTxtField.setDisable(false); passwordTxtField.setText("");
            itemSelect.clear();
            subjCodeISBN.clear();
            postLoginVBox.setVisible(false);
            lockerHBox.setDisable(false);
            lockerHBox.setVisible(false);
            itemChoiceHBox.setDisable(false);
            itemChoiceHBox.setVisible(false);
            postalCodeHBox.setVisible(false);
            optionOneBtn.setDisable(false);
            optionTwoBtn.setDisable(false);
            optionThreeBtn.setDisable(false);
            textArea.clear();
            currLogin = false;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Logout Successful!");
            if (adminLogin){
                alert.setHeaderText("Bye Admin!");
            } else{
                alert.setHeaderText("Bye " + db.getStudent(Security.getCurrStudentLogin()).getName() + "!");
            }
            alert.showAndWait();
            secure.logout();
            adminLogin = false;
            rentalCart = new ArrayList<Book>();
            return;
        }
        String loginID = usernameTxtField.getText();
        String password = passwordTxtField.getText();
        if (!(secure.login(loginID, password) || (loginID.equals("admin") && password.equals("#AB#admin123")))){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Invalid Login!");
            alert.setContentText("Please enter the correct username and password!");
            alert.showAndWait();
            return;
        }
        loginBtn.setText("Logout");
        currLogin = true;
        usernameTxtField.setDisable(true);
        passwordTxtField.setDisable(true);

        if (loginID.equals("admin") && password.equals("#AB#admin123")) {
            //Admin login
            adminLogin = true;
            welcomeLabel.setText("Welcome Admin!");
            walletAmtLabel.setText("");
            itemsCartLabel.setText("");
            postLoginVBox.setVisible(true);
            subjCodeISBNLabel.setText("Enter ISBN: ");
            optionOneBtn.setText("View ALL Books <with ISBN>");
            optionTwoBtn.setText("Search Book Information with Above ISBN");
            optionThreeBtn.setText("Return Book with Above ISBN");
        }
        else {
            //Student login
            adminLogin = false;
            welcomeLabel.setText("Welcome " + db.getStudent(Security.getCurrStudentLogin()).getName() + "!");
            walletAmtLabel.setText(String.format("You have $%.2f in your wallet.\n",  secure.getAccount(Security.getCurrStudentLogin()).getWallet()));
            itemsCartLabel.setText("You have " + rentalCart.size() + " items in your rental cart.");
            postLoginVBox.setVisible(true);
            lockerHBox.setVisible(true);
            subjCodeISBNLabel.setText("Enter Subject Code: ");
            optionOneBtn.setText("View List of Subject Codes");
            optionTwoBtn.setText("View Books by Subject Code / Add Books to Cart");
            optionThreeBtn.setText("View Rental Cart / Checkout");
        }
    }

    public void unlockLocker(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");

        String postal = postalTxtField.getText();
        String password = lockerPassTxtField.getText();
        int lockerNum = -1;

        if (db.getSelfCollection(postal) == null){
            alert.setHeaderText("Invalid Postal Code!");
            alert.setContentText("Please enter a valid postal code!");
            alert.showAndWait();
            return;
        }

        try{
            lockerNum = Integer.parseInt(lockerNumTxtField.getText());
            if (!(lockerNum >= 0 && lockerNum <= db.getSelfCollection(postal).getNumOfLockers()))
                throw new Exception();
        } catch (Exception e){
            alert.setHeaderText("Invalid Locker Number!");
            alert.setContentText("Please enter a valid locker number!");
            alert.showAndWait();
            return;
        }

        if (!db.getSelfCollection(postal).getLocker(lockerNum).getStudentID().equals(Security.getCurrStudentLogin())){
            alert.setHeaderText("Access to locker denied!");
            alert.setContentText("Invalid student accessing locker");
            alert.showAndWait();
            return;
        }

        if (db.getSelfCollection(postal).getLocker(lockerNum).isEmpty()){
            alert.setHeaderText("No items to collect!");
            alert.setContentText(db.getSelfCollection(postal).getLocker(lockerNum).unlockLocker(" "));
            alert.showAndWait();
            return;
        }

        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Locker collection status...");
        alert.setHeaderText("Unlocking locker " + lockerNum + " of " + postal);
        alert.setContentText("Locker collection successful!");
        alert.setContentText(db.getSelfCollection(postal).getLocker(lockerNum).unlockLocker(password));
        alert.setResizable(true);
        alert.showAndWait();
    }

    //New method in Database needed: getBooksDB()
    public void optionOne(ActionEvent actionEvent) {
        itemChoiceHBox.setVisible(false);
        textArea.clear();
        String output = "";
        if (adminLogin){
            String currSubjCode = "";
            int counter = 1;
            output += "Viewing all books:\n";
            for(Book bk: db.getBooksDB()){
                if (!currSubjCode.equals(bk.getSubjectCode())){
                    counter = 1;
                    output += "\n-----------------------" + bk.getSubjectCode() + "-----------------------\n";
                    currSubjCode = bk.getSubjectCode();
                }
                output += String.format("%2d) ",counter) + "<" + bk.getISBN() + "> " + bk.toString() + "\n";
                counter++;
            }
        }
        else{
            itemSelect.clear();
            output += "Viewing all possible subject codes:\n";
            for(String s: db.getPossibleSubjCodes())
                output += s + "\n";
        }
        textArea.setText(output);
    }

    public void optionTwo(ActionEvent actionEvent) {
        textArea.clear();
        String output = "";
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        if (adminLogin){
            itemChoiceHBox.setVisible(false);
            String ISBN = subjCodeISBN.getText();
            if (!Book.checkISBN(ISBN)){
                alert.setHeaderText("Invalid ISBN!");
                alert.setContentText("Please enter a valid ISBN number!");
                alert.showAndWait();
                return;
            }
            Book bk = null;
            try{
                bk = db.getBook(ISBN);
            } catch (Exception e){}

            if (bk == null){
                alert.setHeaderText("No Book Found!");
                alert.setContentText("Please enter a ISBN of a book which exists in the database!");
                alert.showAndWait();
                return;
            } else{
                output += bk.toString();
            }
        } else {
            itemChoiceHBox.setVisible(true);
            addRemoveItemBtn.setText("Add Item to Cart");
            checkoutBtn1.setVisible(false);
            addFlag = true;
            viewBooks = new ArrayList<Book>();
            int count = 1;
            String subjCode = subjCodeISBN.getText();
            viewBooks = db.getBooklistBySubjCode(subjCode, rentalCart);
            if(viewBooks.isEmpty()) {
                output += "No such book exist or all such books have been rented.";
            } else {
                output += "Viewing all " + subjCode + " books:\n";
                for (Book b : viewBooks) {
                    if (rentalCart.contains(b)) continue;
                    output += String.format("%2d) ",count) + b.toString() + "\n";
                    count++;
                }
            }
        }
        textArea.setText(output);
    }

    public void optionThree(ActionEvent actionEvent) {
        textArea.clear();
        String output = "";
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        if (adminLogin){
            itemChoiceHBox.setVisible(false);
            String ISBN = subjCodeISBN.getText();
            if (!Book.checkISBN(ISBN)){
                alert.setHeaderText("Invalid ISBN!");
                alert.setContentText("Please enter a valid ISBN number!");
                alert.showAndWait();
                return;
            }
            Book bk = null;
            try{
                bk = db.getBook(ISBN);
            } catch (Exception e){}

            if (bk == null){
                alert.setHeaderText("No Book Found!");
                alert.setContentText("Please enter a ISBN of a book which exists in the database!");
                alert.showAndWait();
                return;
            } else{
                if (bk.getStudentID() == null || bk.getStudentID().equals("h2100000")){
                    output += secure.getAccount("h2139210").returnBook(bk);
                } else {
                    String msg = secure.getAccount(bk.getStudentID()).addToWallet(bk.getDeposit());
                    output += secure.getAccount(bk.getStudentID()).returnBook(bk) + "\n" + msg;
                }
                db.writeBook("Books.csv");
                secure.writeAccount("Secure.csv");
            }
        } else {
            itemChoiceHBox.setVisible(true);
            addRemoveItemBtn.setText("Remove Cart Item");
            checkoutBtn1.setVisible(true);
            addFlag = false;
            output += "Your rental cart items: \n";
            int itemIndex = 1;
            for(Book b: rentalCart){
                output += String.format("%2d) ",itemIndex) + b.toString() + "\n";
                itemIndex++;
            }
            output += "-------------------------------------\n";
            output += "Total deposit cost: $" + String.format("%.2f",getTotalDeposit(rentalCart)) + "\n";
            output += "Amount in wallet: $" + String.format("%.2f",secure.getAccount(Security.getCurrStudentLogin()).getWallet()) + "\n";
            output += "-------------------------------------\n";
        }
        textArea.setText(output);
    }

    public void addRemoveItem(ActionEvent actionEvent) {
        int choiceNum = -1;
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        try{
            choiceNum = Integer.parseInt(itemSelect.getText());
        } catch (Exception e){
            alert.setHeaderText("Invalid item!");
            alert.setContentText("Enter a valid integer!");
            alert.showAndWait();
            return;
        }

        if (checkOutFlag){
            if (!(choiceNum >= 1 && choiceNum <= sCS.size())){
                alert.setHeaderText("Invalid item!");
                alert.setContentText("Enter a valid integer from 1 to " + sCS.size());
                alert.showAndWait();
                return;
            }
            SelfCollectStn collectionStn = sCS.get(choiceNum - 1);
            Locker collectionLocker = collectionStn.getLocker(collectionStn.getEmptyLockerNum());
            collectionLocker.placeItem(Security.getCurrStudentLogin(), rentalCart);
            double sum = 0;
            textArea.clear();
            String output = "";

            for (Book b : rentalCart) {
                output += secure.getAccount(Security.getCurrStudentLogin()).rentBook(b) + "\n";
                sum += b.getDeposit();
            }
            output += secure.getAccount(Security.getCurrStudentLogin()).deductFromWallet(sum) + "\n";
            db.writeBook("Books.csv");
            secure.writeAccount("Secure.csv");
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Rental Status");
            alert.setHeaderText("Rental Success!");
            alert.setContentText("Amount left in wallet: $" + String.format("%.2f", secure.getAccount(Security.getCurrStudentLogin()).getWallet()));
            alert.showAndWait();
            output += "\n";
            output += "Books placed in " + collectionStn.toString() + "\n" + collectionLocker.toString() + "\n";
            output += "Please collect your books soon! Thank you for using AirBooks!" + "\n";
            textArea.setText(output);

            writeTransaction("Transaction.csv", rentalCart, collectionStn.getPostalCode(), collectionLocker.getLockerNum());
            rentalCart = new ArrayList<Book>();
            walletAmtLabel.setText(String.format("You have $%.2f in your wallet.\n",  secure.getAccount(Security.getCurrStudentLogin()).getWallet()));
            optionOneBtn.setDisable(false);
            optionTwoBtn.setDisable(false);
            optionThreeBtn.setDisable(false);
            lockerHBox.setDisable(false);
            itemsCartLabel.setText("You have " + rentalCart.size() + " items in your rental cart.");
            itemSelect.clear();
            return;
        }

        if (addFlag){
            if (!(choiceNum >= 1 && choiceNum <= viewBooks.size())){
                alert.setHeaderText("Invalid item!");
                alert.setContentText("Enter a valid integer from 1 to " + viewBooks.size());
                alert.showAndWait();
                return;
            }
            Book addBk = viewBooks.get(choiceNum - 1);
            if (getTotalDeposit(rentalCart) + addBk.getDeposit() > secure.getAccount(Security.getCurrStudentLogin()).getWallet()) {
                alert.setHeaderText("Cannot add more books!");
                alert.setContentText("Amount in cart exceeds wallet.");
            }
            else {
                rentalCart.add(addBk);
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Add Successful!");
                alert.setContentText(addBk.getTitle() + " by " + addBk.getAuthor() + " added to cart");
            }
            optionTwo(new ActionEvent());
        }
        else {
            if (rentalCart.isEmpty()){
                alert.setHeaderText("Rental Cart is empty!");
                alert.setContentText("Add some items to your cart!");
                alert.showAndWait();
                return;
            }

            if (!(choiceNum >= 1 && choiceNum <= rentalCart.size())){
                alert.setHeaderText("Invalid item!");
                alert.setContentText("Enter a valid integer from 1 to " + rentalCart.size());
                alert.showAndWait();
                return;
            }

            Book removeBk = rentalCart.get(choiceNum - 1);
            rentalCart.remove(removeBk);
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Removal Successful!");
            alert.setContentText(removeBk.getTitle() + " by " + removeBk.getAuthor() + " removed from cart");
            optionThree(new ActionEvent());
        }
        alert.showAndWait();
        itemsCartLabel.setText("You have " + rentalCart.size() + " items in your rental cart.");
        itemSelect.clear();
    }

    public void enterPostalCode(ActionEvent actionEvent) {
        String output = "";
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        String postal = postalText.getText();
        sCS = db.getNearbySelfCollection(postal);
        if (sCS == null || sCS.isEmpty()){
            alert.setHeaderText("No nearby self collection stations!");
            alert.setContentText("Please try another postal code.");
            alert.showAndWait();
            return;
        }

        int itemIndex = 1;
        output += "Choose a self-collection station from the list below:" + "\n";
        for (SelfCollectStn s : sCS) {
            output += String.format("%2d) ",itemIndex) + s.toString() + "\n";
            itemIndex++;
        }
        textArea.setText(output);
        itemChoiceHBox.setDisable(false);
        checkOutFlag = true;
    }

    public void checkout(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        if (rentalCart.isEmpty()) {
            alert.setHeaderText("Rental cart is empty!");
            alert.setContentText("Add some items to your cart!");
            alert.showAndWait();
            return;
        }
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("You are about to checkout your cart.");
        alert.setContentText("All other options are disabled until the checkout is successful.");
        alert.showAndWait();

        optionOneBtn.setDisable(true);
        optionTwoBtn.setDisable(true);
        optionThreeBtn.setDisable(true);
        lockerHBox.setDisable(true);

        postalCodeHBox.setVisible(true);
        itemChoiceHBox.setDisable(true);
        checkoutBtn1.setVisible(false);
        addRemoveItemBtn.setText("Select Station");
    }

    public double getTotalDeposit(ArrayList<Book> rentalCart){
        double sum = 0;
        for (Book b: rentalCart) { sum += b.getDeposit(); }
        return sum;
    }

    public void writeTransaction(String filename, ArrayList<Book> rentalCart, String stnPostalCode, int lockerNum){
        try{
            PrintWriter output = new PrintWriter(new FileOutputStream(System.getProperty("user.dir") + "/" + filename, true));

            for (Book b: rentalCart){
                output.println(b.getISBN()+","
                        + b.getStudentID() + ","
                        + b.getRentalPeriod() + ","
                        + b.getRentalDate() + ","
                        + stnPostalCode + ","
                        + lockerNum);
            }
            output.close();
        } catch(Exception e){e.printStackTrace(); }
    }
}
