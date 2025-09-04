package utilities;

public class Custom {
	static final String fs = System.getProperty("file.separator");

	public static String getRosettaURL(String rosettaInstance) throws Exception {
		if (rosettaInstance.equals("dev")) {
			return "https://rosetta.develop.lza.tib.eu";
		} else if (rosettaInstance.equals("test")) {
			return "https://rosetta.test.lza.tib.eu";
		} else if (rosettaInstance.equals("prod")) {
			return "https://rosetta.lza.tib.eu";
		} else {
			System.err.println("invalider Wert für rosettaInstance '" + rosettaInstance + "'.");
			throw new Exception();
		}
	}

	public static String getInstitution(String rosettaInstance) throws Exception {
		if (rosettaInstance.equals("dev")) {
			return "ZBM";
		} else if (rosettaInstance.equals("test")) {
			return "ZBMED";
		} else if (rosettaInstance.equals("prod")) {
			return "ZBMED";
		} else {
			System.err.println("invalider Wert für rosettaInstance '" + rosettaInstance + "'.");
			throw new Exception();
		}
	}

	public static String getUsername(String rosettaInstance) throws Exception {
		if (rosettaInstance.equals("dev")) {
			return "SubApp ZB MED";
		} else if (rosettaInstance.equals("test")) {
			return "SubApp ZB MED";
		} else if (rosettaInstance.equals("prod")) {
			return "SubApp ZB MED";
		} else {
			System.err.println("invalider Wert für rosettaInstance '" + rosettaInstance + "'.");
			throw new Exception();
		}
	}

	public static String getSftpUsername() throws Exception {
		String propertyDateiPfad = System.getProperty("user.home").concat(fs).concat("Rosetta_Properties.txt");
		PropertiesManager prop = new PropertiesManager(propertyDateiPfad);
		return prop.readStringFromProperty("sftpUsername");
	}

	public static String getSftpKeyFile() throws Exception {
		String propertyDateiPfad = System.getProperty("user.home").concat(fs).concat("Rosetta_Properties.txt");
		PropertiesManager prop = new PropertiesManager(propertyDateiPfad);
		return prop.readStringFromProperty("sftpKeyFile");
	}

	public static String getSftpKeyPwd() throws Exception {
		String propertyDateiPfad = System.getProperty("user.home").concat(fs).concat("Rosetta_Properties.txt");
		PropertiesManager prop = new PropertiesManager(propertyDateiPfad);
		return prop.readStringFromProperty("sftpKeyPwd");
	}

	public static String getSftpAdresse() throws Exception {
		return "transfer.lza.tib.eu";
	}

	public static String getPassword(String rosettaInstance) throws Exception {
		if (rosettaInstance.equals("dev")) {
			String propertyDateiPfad = System.getProperty("user.home").concat(fs).concat("Rosetta_Properties.txt");
			PropertiesManager prop = new PropertiesManager(propertyDateiPfad);
			return prop.readStringFromProperty("SubApp_Passwort");
		} else if (rosettaInstance.equals("test")) {
			String propertyDateiPfad = System.getProperty("user.home").concat(fs).concat("Rosetta_Properties.txt");
			PropertiesManager prop = new PropertiesManager(propertyDateiPfad);
			return prop.readStringFromProperty("SubApp_Passwort");
		} else if (rosettaInstance.equals("prod")) {
			String propertyDateiPfad = System.getProperty("user.home").concat(fs).concat("Rosetta_Properties.txt");
			PropertiesManager prop = new PropertiesManager(propertyDateiPfad);
			return prop.readStringFromProperty("SubApp_Passwort");
		} else {
			System.err.println("invalider Wert für rosettaInstance '" + rosettaInstance + "'.");
			throw new Exception();
		}
	}

	public static String getIE_WSDL_URL(String rosettaURL) {
		return rosettaURL.concat("/dpsws/repository/IEWebServices?wsdl");
	}

	public static String getSRU_URL(String rosettaInstance) throws Exception {
		return getRosettaURL(rosettaInstance).concat("/search/permanent/ie/sru");
	}

	public static String rosettaInstance(String workflow) throws Exception {
		switch (workflow) {
		case "Test":
			return "test";
		default:
			throw new Exception("Workflow " + workflow + " nicht erkannt");
		}
	}

	public static void main(String[] args) throws Exception {

	}
}
