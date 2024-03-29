package com.example.kinomaniak.controller;

import com.example.kinomaniak.model.Employee;
import com.example.kinomaniak.model.Role;
import com.example.kinomaniak.repository.RoleRepository;
import com.example.kinomaniak.service.AdminService;
import com.example.kinomaniak.service.AuthService;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.converter.LocalDateStringConverter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Stack;

@Component
@FxmlView("accountEditDialog.fxml")
public class AccountEditDialogPresenter {
    @FXML
    private Label errorPrompt;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField surnameTextField;
    @FXML
    private TextField mailTextField;
    @FXML
    private ComboBox<String> roleComboBox;
    private Stage dialogStage;
    private Employee employee;

    private final AdminService adminService;
    private final AuthService authService;

    private TableView<Employee> accountsTable;
    private HomeController homeController;


    @Autowired
    AccountEditDialogPresenter(AdminService adminService, AuthService authService,HomeController homeController){
        this.adminService = adminService;
        this.authService = authService;
        this.homeController = homeController;
    }

    @FXML
    private void initialize(){
        nameTextField.textProperty().addListener(text->errorPrompt.setText(""));
        surnameTextField.textProperty().addListener(text->errorPrompt.setText(""));
        mailTextField.textProperty().addListener(text->errorPrompt.setText(""));
        roleComboBox.valueProperty().addListener(value->errorPrompt.setText(""));

    }
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    public void setData(Employee employee,TableView<Employee> accountsTable) {
        this.employee = employee;
        this.accountsTable = accountsTable;
        updateControls();

    }

    private void updateControls() {
        if(employee.getRole()!=null && employee.getRole().getRoleName().equals("admin")){
            roleComboBox.setDisable(true);
        }
        nameTextField.setText(employee.getName());
        surnameTextField.setText(employee.getSurName());
        mailTextField.setText(employee.getMail());
        roleComboBox.getItems().add("none");
        roleComboBox.getItems().addAll(adminService.getRoles().stream().map(Role::getRoleName).sorted().toList());
        if(employee.getRole() == null)
            roleComboBox.getSelectionModel().select("none");
        else{
            roleComboBox.getSelectionModel().select(employee.getRole().getRoleName());
        }
    }
    @FXML
    private void handleSaveAction(ActionEvent actionEvent) {
        String name = nameTextField.getText();
        String surname = surnameTextField.getText();
        String mail = mailTextField.getText();
        Optional<Role> role =  adminService.getRoleWithName(roleComboBox.getSelectionModel().getSelectedItem());
        boolean refreshLoggedData = employee.getMail().equals(authService.getCurrentlyLoggedEmployee().getMail());
        if(authService.performCredentialsValidation(name,surname) && (mail.equals(employee.getMail()) || authService.performEmailValidation(mail)) &&
                (roleComboBox.getSelectionModel().getSelectedItem().equals("none") || role.isPresent())){
            employee.setName(nameTextField.getText());
            employee.setSurName(surnameTextField.getText());
            employee.setMail(mailTextField.getText());
            if(roleComboBox.getSelectionModel().getSelectedItem().equals("none"))
                employee.setRole(null);
            else {
                role.ifPresent(value -> employee.setRole(value));
            }

            adminService.saveEditedEmployee(employee);
            accountsTable.refresh();
            if(refreshLoggedData){
                authService.refreshCurrentlyLoggedEmployeeData(employee);
                homeController.setCredentialsLabel();
            }

            dialogStage.close();
        }
        else{
            errorPrompt.setText("Wrong data");
        }

    }
    @FXML
    private void handleCancelAction(ActionEvent actionEvent) {
        dialogStage.close();
    }
}
