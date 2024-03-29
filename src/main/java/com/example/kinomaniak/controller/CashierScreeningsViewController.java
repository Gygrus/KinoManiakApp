package com.example.kinomaniak.controller;

import com.example.kinomaniak.model.*;
import com.example.kinomaniak.service.AuthService;
import com.example.kinomaniak.service.CashierService;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@FxmlView("CashierScreeningsView.fxml")
public class CashierScreeningsViewController {

    private final CashierService cashierService;
    private final AuthService authService;

    @FXML
    private TableView<FilmShow> filmShowTable;
    @FXML
    private TableColumn<FilmShow, Movie> movieColumn;
    @FXML
    private TableColumn<FilmShow, ZonedDateTime> dateColumn;
    @FXML
    private TableColumn<FilmShow, Boolean> is3dColumn;
    @FXML
    private TableColumn<FilmShow, Boolean> subtitlesColumn;
    @FXML
    private TableColumn<FilmShow, Hall> hallIdColumn;
    @FXML
    private TableColumn<FilmShow, Integer> sumSeatsColumn;

    @FXML
    private Button toggleFiltersButton;
    @FXML
    private VBox filtersVBox;
    @FXML
    private TextField searchTextField;
    @FXML
    private BorderPane bottomPane;
    @FXML
    private Label priceLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label is3dLabel;
    @FXML
    private Label subtitlesLabel;
    @FXML
    private ScrollPane seatsScrollPane = new ScrollPane();
    @FXML
    private TilePane seatsTilePane = new TilePane();
    @FXML
    private ComboBox<String> hallComboBox;



    private final FxWeaver fxWeaver;

    private boolean rotatedpane =false;
    private ObservableList<FilmShow> filmShows;
    private ObservableList<Movie> movies;
    private HashMap<Integer, Set<Integer>> seats = new HashMap<>();
    private List<Hall> halls;
    private ObservableList<Ticket> tickets;
    private FilmShow currFilmShow;
    private ArrayList<Integer> toBuy;


    public CashierScreeningsViewController(CashierService cashierService, AuthService authService, FxWeaver fxWeaver) {
        this.cashierService = cashierService;
        this.authService = authService;
        this.fxWeaver = fxWeaver;
    }

    @FXML
    private void initialize(){
        loadData();

        createFilmShowTable();
        setColumnsWidthPercentage();
        setUpHallComboBox();
        searchTextField.textProperty().addListener(title-> filmShowTable.setItems(filter()));

    }

    public void loadData(){
        this.filmShows =this.cashierService.getFilmShows();
        this.movies = this.cashierService.getMovies();
        this.tickets = this.cashierService.getAllTickets();

        this.filmShows = FXCollections.observableList(filmShows
                .stream()
                .filter(filmshow -> ZonedDateTime.now().isBefore(filmshow.getDate()))
                .collect(Collectors.toList()));

        this.tickets = FXCollections.observableList(tickets
                .stream()
                .filter(ticket -> ZonedDateTime.now().isBefore(ticket.getFilmShow().getDate()))
                .collect(Collectors.toList()));

        for(int i=0; i < filmShows.size(); i++){
            FilmShow show = filmShows.get(i);
            Set<Integer> seatsShow = new HashSet<Integer>();
            for(int j=0 ; j<show.getHall().getCapacity(); j++ ){
                seatsShow.add(j);
            }

            seats.put(show.getId(), seatsShow);
        }
        for(Ticket ticket: this.tickets){
            seats.get(ticket.getFilmShow().getId()).remove(ticket.getSeatNo());
        }
    }

    public void createFilmShowTable() {
        filmShowTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        movieColumn.setCellValueFactory(new PropertyValueFactory<>("movie"));
        movieColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            public void updateItem(Movie movie, boolean empty) {
                super.updateItem(movie, empty);
                if (empty || movie == null) {
                    setText("");
                } else {
                    setText(movie.getTitle());
                }
            }
        });
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            public void updateItem(ZonedDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText("");
                } else {
                    String tmp = date.toString();
                    String res = tmp.substring(0, 10) + "   " + tmp.substring(11, 16);
                    setText(res);
                }
            }
        });
        is3dColumn.setCellValueFactory(new PropertyValueFactory<>("is3D"));
        subtitlesColumn.setCellValueFactory(new PropertyValueFactory<>("withSubtitles"));
        hallIdColumn.setCellValueFactory(new PropertyValueFactory<>("hall"));
        hallIdColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            public void updateItem(Hall hall, boolean empty) {
                super.updateItem(hall, empty);
                if (empty || hall == null) {
                    setText("");
                } else {
                    setText(hall.getHallNo().toString());
                }
            }
        });
        sumSeatsColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        sumSeatsColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            public void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText("");
                } else {
                    setText(String.valueOf(seats.get(id).size()));
                }
            }
        });

        filmShowTable.setItems(filmShows);

        filmShowTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            if(oldValue == null){
                filmShowTable.setPrefHeight(filmShowTable.getPrefHeight() - 100.0);
                bottomPane.setPrefHeight(bottomPane.getPrefHeight()+ 100.0);
                bottomPane.setVisible(true);
                bottomPane.setManaged(true);
            }

            if(newValue == null){
                filmShowTable.setPrefHeight(filmShowTable.getPrefHeight() + 100.0);
                bottomPane.setPrefHeight(bottomPane.getPrefHeight() - 100.0);
                bottomPane.setVisible(false);
                bottomPane.setManaged(false);
            }else{
                titleLabel.textProperty().bind(new SimpleStringProperty(newValue.getMovie().getTitle()));
                priceLabel.textProperty().bind(new SimpleStringProperty(String.valueOf(newValue.getTicketPrice()) + " zł"));
                titleLabel.setWrapText(true);
                String is3d = "";
                if(newValue.getIs3D()) is3d = "Tak";
                else is3d = "Nie";
                is3dLabel.textProperty().setValue(is3d);
                String sub = "";
                if(newValue.getWithSubtitles()) sub = "Tak";
                else sub = "Nie";
                subtitlesLabel.textProperty().setValue(sub);

                seatsScrollPane.setFitToWidth(false);
                this.toBuy = new ArrayList<>();
                this.currFilmShow = newValue;
                this.seatsTilePane = new TilePane();

                seatsTilePane.paddingProperty().bind(Bindings.createObjectBinding(() -> new Insets(0, 0, 0, Math.max((seatsScrollPane.getViewportBounds().getWidth())/2 -550, 0)), seatsScrollPane.viewportBoundsProperty()));

//                seatsTilePane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
//                        seatsScrollPane.getViewportBounds().getWidth() - 10));

                seatsTilePane.setPrefWidth(550);
                this.seatsTilePane.getStyleClass().add("full");
                for(int i=0; i < newValue.getHall().getCapacity(); i++){
                    Button button = new Button(String.valueOf(i+1));
                    button.getStyleClass().clear();
                    button.getStyleClass().add("button-seats");
                    if(i < 9){
                        button.setPadding(new Insets(5, 15, 5, 20));
                    }
                    else if(i < 99) {
                        button.setPadding(new Insets(5, 15, 5, 17));
                    }
                    else{
                        button.setPadding(new Insets(5, 12, 5, 11));
                    }

                    if(!this.seats.get(newValue.getId()).contains(i)){
                        button.setStyle("-fx-background-color: #c40018");
                    }
                    else{
                        button.getStyleClass().add("button-seats-click");
                    }

                    Integer finalI = i;
                    button.setOnAction(event -> {
                        if(this.toBuy.contains(finalI)){
                            toBuy.remove(finalI);
                            button.setStyle("-fx-background-color: #8ea6b4;");
                            popSeat(button);
                        }
                        else if(this.seats.get(newValue.getId()).contains(finalI)){
                            toBuy.add(finalI);
                            button.setStyle("-fx-background-color: #4a772f;");
                            popSeat(button);
                        }
                        else{
                            rotateButton(button);
                        }
                    });
                    this.seatsTilePane.getChildren().add(button);
                }
                seatsScrollPane.setContent(this.seatsTilePane);
            }
        } );
    }

    @FXML
    private void buyTickets(){
        filmShowTable.setPrefHeight(filmShowTable.getPrefHeight() + 100.0);
        bottomPane.setPrefHeight(bottomPane.getPrefHeight() - 100.0);

        for(Integer seat: toBuy){
            this.cashierService.reserveTicketsForGivenFilm(currFilmShow, authService.getCurrentlyLoggedEmployee(), seat);
        }
        initialize();
    }

    private void setUpHallComboBox() {
        this.halls = cashierService.getHalls();
        if(this.hallComboBox.getItems().size() == 0){
            this.hallComboBox.getItems().add("not specified");
            this.hallComboBox.getItems().addAll((halls.stream().map(Hall::getHallNo).sorted().map(String::valueOf)).toList());
            this.hallComboBox.getSelectionModel().selectFirst();
            hallComboBox.valueProperty().addListener(genre-> filmShowTable.setItems(filter()));
        }

    }

    private void setColumnsWidthPercentage() {
        movieColumn.prefWidthProperty().bind(filmShowTable.widthProperty().multiply(0.3));
        dateColumn.prefWidthProperty().bind(filmShowTable.widthProperty().multiply(0.2));
        is3dColumn.prefWidthProperty().bind(filmShowTable.widthProperty().multiply(0.1));
        subtitlesColumn.prefWidthProperty().bind(filmShowTable.widthProperty().multiply(0.1));
        hallIdColumn.prefWidthProperty().bind(filmShowTable.widthProperty().multiply(0.1));
        sumSeatsColumn.prefWidthProperty().bind(filmShowTable.widthProperty().multiply(0.15));
    }

    private ObservableList<FilmShow> filter() {
        removeSelection();
        List<String> filteredMovies =  FXCollections.observableList(movies
                .stream()
                .map(Movie::getTitle)
                .filter(title -> title.toLowerCase().contains(searchTextField.getText().toLowerCase()))
                .toList());

        return FXCollections.observableList(filmShows
                .stream()
                .filter(filmshow -> filteredMovies.contains(filmshow.getMovie().getTitle()))
                .filter(filmShow -> {
                    if(hallComboBox.getValue().equals("not specified"))
                        return true;
                    return filmShow.getHall().getHallNo().equals(Integer.valueOf(hallComboBox.getValue()));
                })
                .collect(Collectors.toList()));

    }

    public void toggleFilters() {
        filtersVBox.setManaged(!filtersVBox.managedProperty().getValue());
        filtersVBox.setVisible(!filtersVBox.visibleProperty().getValue());
    }

    @FXML
    private void resetFilters(){

        searchTextField.setText("");
        hallComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void removeSelection() {
        filmShowTable.getSelectionModel().clearSelection();
    }

    public void searchMovieTitle(String movieTitle){
        searchTextField.setText(movieTitle);
    }

    public void searchHallNo(int hallNo){
        hallComboBox.getSelectionModel().select(String.valueOf(hallNo));
    }

    private void popSeat(Button btn){
        ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
        st.setToX(1.2);
        st.setToY(1.2);
        st.setRate(1.5);
        st.setCycleCount(1);
        st.play();
        st.setOnFinished(event -> {
            ScaleTransition st2 = new ScaleTransition(Duration.millis(200), btn);
            st2.setToX(1);
            st2.setToY(1);
            st2.setRate(1.5);
            st2.setCycleCount(1);
            st2.play();
        });
    }

    public void rotateButton(Button btn){
        if(rotatedpane ==false){
            rotatedpane =true;
            RotateTransition rt=new RotateTransition(Duration.millis(60),btn);
            rt.setByAngle(45);
            rt.setCycleCount(2);
            rt.setAutoReverse(true);
            rt.play();

            rt.setOnFinished(event -> {
                RotateTransition rt2=new RotateTransition(Duration.millis(60),btn);
                rt2.setByAngle(-45);
                rt2.setCycleCount(2);
                rt2.setAutoReverse(true);
                rt2.play();
                rt2.setOnFinished(event1 -> rotatedpane =false);
            });
        }
    }
}
