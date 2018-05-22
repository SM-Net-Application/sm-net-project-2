package com.sm.net.apc;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledExecutorService;

import com.sm.net.apc.model.AmazonPrice;
import com.sm.net.apc.model.AmazonProduct;
import com.sm.net.apc.view.MainView;
import com.sm.net.simple.h2.H2DataTypes;
import com.sm.net.simple.h2.OperationBuilder;
import com.sm.net.simple.h2.SimpleH2Column;
import com.sm.net.simple.h2.SimpleH2Database;
import com.sm.net.simple.h2.SimpleH2ResultSet;
import com.sm.net.simple.h2.SimpleH2Schema;
import com.sm.net.simple.h2.SimpleH2Table;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	public final File ICON = setIconApp();
	public static final File DOWN = setDownImage();
	public static final File UP = setUpImage();
	public static final File PRICE = setPriceImage();

	private ScheduledExecutorService executorService;

	@Override
	public void start(Stage primaryStage) {

		this.executorService = null;

		SimpleH2Database database = createDatabase();
		buildDatabase(database);
		loadMainView(primaryStage, database);
	}

	private static File setPriceImage() {
		String path = System.getProperty("user.dir") + File.separatorChar + "resources" + File.separatorChar + "images"
				+ File.separatorChar + "price.png";

		return new File(path);
	}

	private static File setUpImage() {
		String path = System.getProperty("user.dir") + File.separatorChar + "resources" + File.separatorChar + "images"
				+ File.separatorChar + "up.png";

		return new File(path);
	}

	private static File setDownImage() {
		String path = System.getProperty("user.dir") + File.separatorChar + "resources" + File.separatorChar + "images"
				+ File.separatorChar + "down.png";

		return new File(path);
	}

	private void loadMainView(Stage primaryStage, SimpleH2Database database) {

		try {

			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(Main.class.getResource("view/MainView.fxml"));

			Scene scene = new Scene((AnchorPane) fxmlLoader.load());
			// scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);

			primaryStage.setTitle("Amazon PriceCheck 1.0");
			primaryStage.getIcons().add(new Image(ICON.toURI().toString()));
			primaryStage.setMaximized(true);

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent event) {
					if (executorService != null)
						executorService.shutdown();
					System.exit(0);
				}
			});

			MainView controller = (MainView) fxmlLoader.getController();
			controller.setDatabase(database);
			controller.setExecutorService(this.executorService);
			controller.init();

			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void buildDatabase(SimpleH2Database database) {

		SimpleH2Schema schema = new SimpleH2Schema("apc", true);

		SimpleH2Table product = new SimpleH2Table("product", schema, true);
		SimpleH2Column id = new SimpleH2Column("id", H2DataTypes.INT, true, true);
		SimpleH2Column code = new SimpleH2Column("code", H2DataTypes.VARCHAR, true, 500);
		SimpleH2Column productName = new SimpleH2Column("product_name", H2DataTypes.VARCHAR, true, 1000);
		SimpleH2Column imageUrl = new SimpleH2Column("image_url", H2DataTypes.VARCHAR, true, 1000);
		product.addColumn(id, code, productName, imageUrl);

		SimpleH2Table priceCheck = new SimpleH2Table("price_check", schema, true);
		SimpleH2Column creationDate = new SimpleH2Column("creation_date", H2DataTypes.DATE, true);
		SimpleH2Column price = new SimpleH2Column("price", H2DataTypes.DECIMAL, true);
		SimpleH2Column priceOld = new SimpleH2Column("price_old", H2DataTypes.DECIMAL, true);
		SimpleH2Column idProduct = new SimpleH2Column("id_product", H2DataTypes.INT, true);
		SimpleH2Column status = new SimpleH2Column("status", H2DataTypes.INT, true);
		priceCheck.addColumn(id, creationDate, price, priceOld, idProduct, status);

		database.createSchema(schema);
		database.createTable(product);
		database.createTable(priceCheck);
	}

	private SimpleH2Database createDatabase() {

		String folder = System.getProperty("user.dir");
		folder += File.separatorChar;
		folder += "resources";
		folder += File.separatorChar;
		folder += "database";

		File folderFile = new File(folder);
		folderFile.mkdirs();

		return new SimpleH2Database(folderFile, "amazonPriceCheck", "admin", "", true);
	}

	public static ObservableList<AmazonProduct> getListProduct(SimpleH2Database database) {

		ObservableList<AmazonProduct> list = FXCollections.observableArrayList();

		OperationBuilder selectionCriteria = new OperationBuilder("apc", "product");
		selectionCriteria.setSelection("product", "id");
		selectionCriteria.setSelection("product", "code");
		selectionCriteria.setSelection("product", "product_name");
		selectionCriteria.setSelection("product", "image_url");

		SimpleH2ResultSet selection = database.runSelection(selectionCriteria.buildSelection());
		ResultSet resultSet = selection.getResultSet();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String code = resultSet.getString("code");
				String productName = resultSet.getString("product_name");
				String imageUrl = resultSet.getString("image_url");

				list.add(new AmazonProduct(id, code, productName, imageUrl));
			}
		} catch (SQLException e) {
		}
		selection.close();
		return list;
	}

	public static ObservableList<AmazonPrice> getListPrice(SimpleH2Database database, Integer idProduct) {

		ObservableList<AmazonPrice> list = FXCollections.observableArrayList();

		OperationBuilder selectionCriteria = new OperationBuilder("apc", "price_check");
		selectionCriteria.setSelection("price_check", "id");
		selectionCriteria.setSelection("price_check", "creation_date");
		selectionCriteria.setSelection("price_check", "price");
		selectionCriteria.setSelection("price_check", "price_old");
		selectionCriteria.setSelection("price_check", "id_product");
		selectionCriteria.setSelection("price_check", "status");
		selectionCriteria.setConditionEquals("id_product", idProduct);

		SimpleH2ResultSet selection = database.runSelection(selectionCriteria.buildSelection());
		ResultSet resultSet = selection.getResultSet();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				Date creationData = resultSet.getDate("creation_date");
				BigDecimal price = resultSet.getBigDecimal("price");
				BigDecimal priceOld = resultSet.getBigDecimal("price_old");
				int idProduct2 = resultSet.getInt("id_product");
				int status = resultSet.getInt("status");

				AmazonPrice amazonPrice = new AmazonPrice(id, creationData, price, priceOld, idProduct2, status);

				list.add(amazonPrice);
			}
		} catch (SQLException e) {
		}
		selection.close();
		return list;
	}

	private File setIconApp() {

		String path = System.getProperty("user.dir") + File.separatorChar + "resources" + File.separatorChar + "images"
				+ File.separatorChar + "icon.png";

		return new File(path);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
