package com.example.kinomaniak.controller;

import com.example.kinomaniak.model.Hall;
import com.example.kinomaniak.model.Movie;
import com.example.kinomaniak.service.CashierService;
import com.example.kinomaniak.service.ManagerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@FxmlView("managerScreeningView.fxml")
public class ManagerScreeningViewController {
    @FXML
    public Button addScreeningBtn;
    @FXML
    public Button removeScreeningBtn;
    @FXML
    public VBox addScreeningForm;
    @FXML
    public VBox removeScreeningForm;
    @FXML
    public ComboBox<String> selectedHall;
    @FXML
    public ComboBox<String> selectedMovie;
    @FXML
    public DatePicker selectedDate;
    @FXML
    public Spinner<Integer> selectedHour;
    @FXML
    public Spinner<String> selectedMinute;
    @FXML
    public Spinner<Integer> selectedPriceNumbers;
    @FXML
    public Spinner<String> selectedPriceDecimals;
    @FXML
    public CheckBox selectedSubtitle;
    @FXML
    public CheckBox selected3D;
    @FXML
    public Label addScreeningErrorPrompt;

    private final ManagerService managerService;
    private final CashierService cashierService;
    public List<Hall> halls;
    public List<Movie> movies;


    private SpinnerValueFactory<Integer> hourValueFactory;
    private SpinnerValueFactory<String> minuteValueFactory;
    private SpinnerValueFactory<Integer> numbersValueFactory;
    private SpinnerValueFactory<String> decimalsValueFactory;

    @FXML
    public void toggleAddScreening(){
        removeScreeningForm.setManaged(false);
        removeScreeningForm.setVisible(false);
        addScreeningForm.setManaged(true);
        addScreeningForm.setVisible(true);
    }

    @FXML
    public void toggleRemoveScreening(){
        addScreeningForm.setManaged(false);
        addScreeningForm.setVisible(false);
        removeScreeningForm.setManaged(true);
        removeScreeningForm.setVisible(true);
    }

    @FXML
    public void submitAddingScreening() {
        if(inputValidation()){
            Hall hallForFilmShow = halls.stream().filter(hall -> (Objects.equals(hall.getHallNo(), Integer.valueOf(selectedHall.getValue())))).findAny().get();
            Movie movieForFilmShow = movies.stream().filter(movie -> (movie.getTitle().equals(selectedMovie.getValue()))).findAny().get();

            int hours = selectedHour.getValue();
            int minutes = Integer.parseInt(selectedMinute.getValue());

            LocalDateTime localDateTime = LocalDateTime.of(selectedDate.getValue().getYear(), selectedDate.getValue().getMonthValue(),
                    selectedDate.getValue().getDayOfMonth(),hours , minutes, 0);

            ZonedDateTime time = ZonedDateTime.of(localDateTime, ZoneId.of("Europe/Warsaw"));
            Double ticketPrice = Double.valueOf(selectedPriceNumbers.getValue() + "." + selectedPriceDecimals.getValue());

            if(managerService.existFilmShowWithGivenHallAndTime(hallForFilmShow, time)){
                managerService.addFilmShow(hallForFilmShow, movieForFilmShow, time, ticketPrice,
                        selectedSubtitle.isSelected(), selected3D.isSelected());
            } else {
                addScreeningErrorPrompt.setText("Chosen hall is reserved at chosen time for different screening!");

            }
        }
    }

    @FXML
    public void initialize(){
        setUpHallComboBox();
        setUpMovieComboBox();
        setUpScreeningDate();
        setUpTicketPrice();
    }

    public ManagerScreeningViewController(ManagerService managerService, CashierService cashierService) {
        this.managerService = managerService;
        this.cashierService = cashierService;
    }

    public void setUpHallComboBox(){
        this.halls = managerService.getHalls();
        selectedHall.getItems().add("not specified");
        selectedHall.getItems().addAll(halls.stream().map(Hall::getHallNo).sorted().map(Objects::toString).toList());
        selectedHall.getSelectionModel().selectFirst();
    }

    public void setUpMovieComboBox(){
        this.movies = cashierService.getMovies();
        selectedMovie.getItems().add("not specified");
        selectedMovie.getItems().addAll(movies.stream().map(Movie::getTitle).sorted().toList());
        selectedMovie.getSelectionModel().selectFirst();
    }

    public void setUpScreeningDate(){
        selectedDate.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0 );
            }
        });
        selectedDate.setValue(LocalDate.now());

        hourValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,23, 12);
        selectedHour.setValueFactory(hourValueFactory);
        selectedHour.setEditable(true);

        List<String> minutes = new ArrayList<>();

        for (int i = 0; i <100; i+=5){
            minutes.add(i < 10 ? "0" + i : "" + i );
        }

        ObservableList<String> minutesObservable = FXCollections.observableList(minutes);

        minuteValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(minutesObservable);
        selectedMinute.setValueFactory(minuteValueFactory);
        selectedMinute.setEditable(true);
    }

    public void setUpTicketPrice(){
        List<String> decimals = new ArrayList<>();

        for (int i = 0; i < 100; i++){
            decimals.add(i < 10 ? "0" + i : "" + i );
        }

        ObservableList<String> decimalsObservable = FXCollections.observableList(decimals);

        decimalsValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(decimalsObservable);
        decimalsValueFactory.setValue("50");
        selectedPriceDecimals.setValueFactory(decimalsValueFactory);
        selectedPriceDecimals.setEditable(true);

        numbersValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,60, 15);
        selectedPriceNumbers.setValueFactory(numbersValueFactory);
        selectedPriceNumbers.setEditable(true);

    }

    public boolean inputValidation(){
        String errorMsg = "";
        System.out.println(selectedHall.getValue());
        if(selectedHall.getValue().equals("not specified")){
            System.out.println("hall");
            errorMsg += "\t\t Hall is not specified!!";
        }
        if(selectedMovie.getValue().equals("not specified")){
            System.out.println("movie");

            errorMsg += "\t\t Movie is not specified!!";
        }
        addScreeningErrorPrompt.setText(errorMsg);

        return addScreeningErrorPrompt.getText().isBlank();

    }

}
