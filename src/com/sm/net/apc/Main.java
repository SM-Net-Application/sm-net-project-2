package com.sm.net.apc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ScheduledExecutorService;

import com.sm.net.apc.model.AmazonList;
import com.sm.net.apc.model.AmazonPrice;
import com.sm.net.apc.model.AmazonProduct;
import com.sm.net.apc.model.Setting;
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

	public static final File ICON = setIconApp();
	public static final File DOWN = setDownImage();
	public static final File UP = setUpImage();
	public static final File PRICE = setPriceImage();

	public static Setting ext = null;
	public static Setting min = null;

	public static String appName = "Amazon PriceCheck";
	public static String version = "1.0";

	private ScheduledExecutorService executorService;

	@Override
	public void start(Stage primaryStage) {

		this.executorService = null;

		SimpleH2Database database = createDatabase();
		buildDatabase(database);
		loadSettings(database);
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
			controller.setMainViewStage(primaryStage);
			controller.init();

			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void loadSettings(SimpleH2Database database) {

		ObservableList<Setting> settings = getSettings(database);

		if (settings.size() > 0) {

			for (Setting setting : settings) {
				switch (setting.getName().get()) {
				case "ext":
					Main.ext = setting;
					break;
				case "min":
					Main.min = setting;
					break;
				default:
					break;
				}
			}

		} else {
			saveDefaultSettings(database);
			loadSettings(database);
		}
	}

	public static void updateSettings(SimpleH2Database database, String ext, String min) {
		updateSettingsExt(database, ext);
		updateSettingsMin(database, min);
		loadSettings(database);
	}

	private static void updateSettingsExt(SimpleH2Database database, String ext) {

		OperationBuilder op = new OperationBuilder("apc", "settings");
		op.setColumnValue("value", ext);
		op.setConditionEquals("id", Main.ext.getId().get());

		database.runOperation(op.buildUpdate());
	}

	private static void updateSettingsMin(SimpleH2Database database, String min) {

		OperationBuilder op = new OperationBuilder("apc", "settings");
		op.setColumnValue("value", min);
		op.setConditionEquals("id", Main.min.getId().get());

		database.runOperation(op.buildUpdate());
	}

	public static void saveDefaultSettings(SimpleH2Database database) {
		saveDefaultSettingsExt(database);
		saveDefaultSettingsMin(database);
	}

	private static void saveDefaultSettingsMin(SimpleH2Database database) {
		OperationBuilder op = new OperationBuilder("apc", "settings");
		op.setColumnValue("name", "min");
		op.setColumnValue("value", "60");

		database.runOperation(op.buildInsert());
	}

	private static void saveDefaultSettingsExt(SimpleH2Database database) {

		OperationBuilder op = new OperationBuilder("apc", "settings");
		op.setColumnValue("name", "ext");
		op.setColumnValue("value", "de");

		database.runOperation(op.buildInsert());
	}

	private void buildDatabase(SimpleH2Database database) {

		SimpleH2Schema schema = new SimpleH2Schema("apc", true);

		SimpleH2Table product = new SimpleH2Table("product", schema, true);
		SimpleH2Column id = new SimpleH2Column("id", H2DataTypes.INT, true, true);
		SimpleH2Column code = new SimpleH2Column("code", H2DataTypes.VARCHAR, true, 500);
		SimpleH2Column productName = new SimpleH2Column("product_name", H2DataTypes.VARCHAR, true, 1000);
		SimpleH2Column imageUrl = new SimpleH2Column("image_url", H2DataTypes.VARCHAR, true, 1000);
		SimpleH2Column idList = new SimpleH2Column("id_list", H2DataTypes.INT, true);
		SimpleH2Column priceAlert = new SimpleH2Column("price_alert", H2DataTypes.DECIMAL, true);
		product.addColumn(id, code, productName, imageUrl, idList, priceAlert);

		SimpleH2Table priceCheck = new SimpleH2Table("price_check", schema, true);
		SimpleH2Column creationDate = new SimpleH2Column("creation_date", H2DataTypes.DATE, true);
		SimpleH2Column price = new SimpleH2Column("price", H2DataTypes.DECIMAL, true);
		SimpleH2Column priceOld = new SimpleH2Column("price_old", H2DataTypes.DECIMAL, true);
		SimpleH2Column idProduct = new SimpleH2Column("id_product", H2DataTypes.INT, true);
		SimpleH2Column status = new SimpleH2Column("status", H2DataTypes.INT, true);
		priceCheck.addColumn(id, creationDate, price, priceOld, idProduct, status);

		SimpleH2Table list = new SimpleH2Table("list", schema, true);
		SimpleH2Column name = new SimpleH2Column("name", H2DataTypes.VARCHAR, true, 1000);
		list.addColumn(id, name);

		SimpleH2Table settings = new SimpleH2Table("settings", schema, true);
		SimpleH2Column settingName = new SimpleH2Column("name", H2DataTypes.VARCHAR, true, 1000);
		SimpleH2Column settingValue = new SimpleH2Column("value", H2DataTypes.VARCHAR, true, 1000);
		settings.addColumn(id, settingName, settingValue);

		database.createSchema(schema);
		database.createTable(product);
		database.createTable(priceCheck);
		database.createTable(list);
		database.createTable(settings);
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

	public static ObservableList<AmazonProduct> getListProduct(SimpleH2Database database, Integer idList) {

		ObservableList<AmazonProduct> list = FXCollections.observableArrayList();

		OperationBuilder selectionCriteria = new OperationBuilder("apc", "product");
		selectionCriteria.setSelection("product", "id");
		selectionCriteria.setSelection("product", "code");
		selectionCriteria.setSelection("product", "product_name");
		selectionCriteria.setSelection("product", "image_url");
		selectionCriteria.setSelection("product", "id_list");
		selectionCriteria.setSelection("product", "price_alert");

		if (idList.intValue() > -2)
			selectionCriteria.setConditionEquals("id_list", idList);

		SimpleH2ResultSet selection = database.runSelection(selectionCriteria.buildSelection());
		ResultSet resultSet = selection.getResultSet();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String code = resultSet.getString("code");
				String productName = resultSet.getString("product_name");
				String imageUrl = resultSet.getString("image_url");
				int idListRecord = resultSet.getInt("id_list");
				BigDecimal priceAlert = resultSet.getBigDecimal("price_alert");

				list.add(new AmazonProduct(id, code, productName, imageUrl, idListRecord, priceAlert));
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

		SimpleH2ResultSet selection = database
				.runSelection(selectionCriteria.buildSelection() + " ORDER BY creation_date DESC, id DESC");
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

	public static ObservableList<AmazonList> getListList(SimpleH2Database database) {

		ObservableList<AmazonList> list = FXCollections.observableArrayList();

		OperationBuilder selectionCriteria = new OperationBuilder("apc", "list");
		selectionCriteria.setSelection("list", "id");
		selectionCriteria.setSelection("list", "name");

		SimpleH2ResultSet selection = database.runSelection(selectionCriteria.buildSelection() + " ORDER BY name ASC");
		ResultSet resultSet = selection.getResultSet();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");

				list.add(new AmazonList(id, name));
			}
		} catch (SQLException e) {
		}
		selection.close();
		return list;
	}

	public static ObservableList<Setting> getSettings(SimpleH2Database database) {

		ObservableList<Setting> list = FXCollections.observableArrayList();

		OperationBuilder selectionCriteria = new OperationBuilder("apc", "settings");
		selectionCriteria.setSelection("settings", "id");
		selectionCriteria.setSelection("settings", "name");
		selectionCriteria.setSelection("settings", "value");

		SimpleH2ResultSet selection = database.runSelection(selectionCriteria.buildSelection());
		ResultSet resultSet = selection.getResultSet();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				String value = resultSet.getString("value");

				list.add(new Setting(id, name, value));
			}
		} catch (SQLException e) {
		}
		selection.close();
		return list;
	}

	private static File setIconApp() {

		String path = System.getProperty("user.dir") + File.separatorChar + "resources" + File.separatorChar + "images"
				+ File.separatorChar + "icon.png";

		return new File(path);
	}

	public static String getImagePath(int id) {

		String path = System.getProperty("user.dir") + File.separatorChar + "resources" + File.separatorChar + "fotos"
				+ File.separatorChar;
		path += id + ".png";

		return path;
	}

	public static void main(String[] args) {
		launch(args);
	}

	public static void writeLog(String text) {

		BufferedWriter writer = null;
		try {
			String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
			File logFile = new File("logAmazonPriceCheck.txt");

			writer = new BufferedWriter(new FileWriter(logFile, true));
			writer.write(timeLog + " - " + text.replace("\n", " ") + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
			}
		}
	}
}
