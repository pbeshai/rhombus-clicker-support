package ca.ubc.clickers.driver;

import java.io.IOException;

import ca.ubc.clickers.ClickerApp;
import ca.ubc.clickers.driver.v1.IClickerDriverV1;
import ca.ubc.clickers.driver.v2.IClickerDriverV2;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDDeviceNotFoundException;
import com.codeminders.hidapi.HIDManager;

public class HIDManagerClickerApp extends HIDManager {
	private static final int ICLICKER_VENDOR_ID = 6273;
	private static final int BASE_STATION_PRODUCT_ID = 336;
	private static final String V1_BASE_STATION_MANUFACTURER = "I-Clicker USB Base";
	private static final String V2_BASE_STATION_MANUFACTURER = "iclicker";
	
	private ClickerApp app;
	
	public HIDManagerClickerApp(ClickerApp app) throws IOException {
       super();
       this.app = app;
    }
	
	public IClickerDriver getIClickerDriver() {
		// try to get the device
		HIDDevice device = null;
		
		IClickerDriver driver = null;
		
		try {
			for (HIDDeviceInfo info : listDevices()) {
				if (isV1BaseStation(info)) {
					System.err.println("Found V1 IClicker Base Station");
					device = openBaseStation();
					driver = new IClickerDriverV1(device);
					break;
				} else if (isV2BaseStation(info)) {
					System.err.println("Found V2 IClicker Base Station");
					device = openBaseStation();
					driver = new IClickerDriverV2(device);
					break;
				}
			}	
		} catch (IOException e) { }
		
		return driver;
	}
	
	public boolean isBaseStation(HIDDeviceInfo device) {
		 return (device.getVendor_id() == ICLICKER_VENDOR_ID && device.getProduct_id() == BASE_STATION_PRODUCT_ID);
	}
	
	public boolean isV1BaseStation(HIDDeviceInfo device) {
		return isBaseStation(device) && (V1_BASE_STATION_MANUFACTURER.equals(device.getManufacturer_string()));
	}
	
	public boolean isV2BaseStation(HIDDeviceInfo device) {
		return isBaseStation(device) && (V2_BASE_STATION_MANUFACTURER.equals(device.getManufacturer_string()));
	}
	
	@Override
	public void deviceAdded(HIDDeviceInfo device) {
		if (device.getVendor_id() == ICLICKER_VENDOR_ID && device.getProduct_id() == BASE_STATION_PRODUCT_ID) {
			if (V1_BASE_STATION_MANUFACTURER.equals(device.getManufacturer_string())) {
				app.baseStationAdded();
			} else if (V2_BASE_STATION_MANUFACTURER.equals(device.getManufacturer_string())) {
				app.baseStationAdded();
			}
		}
	}

	@Override
	public void deviceRemoved(HIDDeviceInfo device) {
		if (device.getVendor_id() == ICLICKER_VENDOR_ID && device.getProduct_id() == BASE_STATION_PRODUCT_ID) {
			if (V1_BASE_STATION_MANUFACTURER.equals(device.getManufacturer_string())) {
				app.baseStationRemoved();
			} else if (V2_BASE_STATION_MANUFACTURER.equals(device.getManufacturer_string())) {
				app.baseStationRemoved();
			}
		}
	}
	
	public HIDDevice openBaseStation() throws HIDDeviceNotFoundException, IOException {
		return openById(ICLICKER_VENDOR_ID, BASE_STATION_PRODUCT_ID, null);
	}

}
