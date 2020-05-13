package com.appian.robot.demo;

import java.io.Serializable;

import com.appian.robot.demo.commons.IBM3270AppManager;
import com.appian.robot.demo.commons.IBM3270Constants;
import com.appian.robot.demo.pages.ConstantsTexts;
import com.appian.robot.demo.pages.NetViewPage;
import com.appian.rpa.snippet.IBM3270Commons;
import com.appian.rpa.snippet.TextInScreen;
import com.appian.rpa.snippet.clients.PCOMCommonsExtended;
import com.appian.rpa.snippet.clients.WC3270CommonsExtended;
import com.appian.rpa.snippet.page.RemotePage;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * Robot for managing 3270 terminals
 */
@Robot
public class Robot3270 implements IRobot {

	/**
	 * Jidoka server instance
	 */
	private IJidokaServer<Serializable> server;

	/**
	 * Client Module Instance
	 */
	protected IClient client;

	/**
	 * IBM3270Commons snippet instance
	 */
	private IBM3270Commons ibm3270Commons;

	/**
	 * IBM3270AppManager instance
	 */
	private IBM3270AppManager appManager;

	/**
	 * Current screen
	 */
	public RemotePage currentPage;

	/**
	 * App Name
	 */
	public String appName;

	/**
	 * Process Name
	 */
	public String processName;

	/**
	 * Startup method. This <code>startUp</code> method is called prior to calling
	 * the first workflow method defined
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean startUp() throws Exception {

		server = (IJidokaServer<Serializable>) JidokaFactory.getServer();

		client = IClient.getInstance(this);

		return true;
	}

	/**
	 * Action 'Init'.
	 * <p>
	 * Initializes Jidoka modules. Instances of the emulator type passed as a
	 * parameter are loaded
	 */
	public void init() {

		try {

			String emulator = server.getParameters().get("Emulator");

			if (emulator.equals("wc3270")) {
				ibm3270Commons = new WC3270CommonsExtended(client, this);
				appName = IBM3270Constants.APP_NAME_WC3270;
				processName = IBM3270Constants.PROCESS_NAME_WC3270;
			} else {
				ibm3270Commons = new PCOMCommonsExtended(client, this);
				appName = IBM3270Constants.APP_NAME_PCOMM;
				processName = IBM3270Constants.PROCESS_NAME_PCOMM;
			}

			appManager = new IBM3270AppManager(this);

			server.debug("Robot initialized");
		} catch (Exception e) {
			throw new JidokaFatalException("Error initializing");
		}
	}

	/**
	 * Action 'Open 3270' terminal
	 */
	public void open3270() {

		server.info("Opening 3270 terminal");

		appManager.openIBM3270(appName, ibm3270Commons.getWindowTitleRegex());
		client.pause(1000);

		server.sendScreen("Screenshot after opening the terminal");
	}

	/**
	 * Action 'Validate page'.
	 */
	public void validateMainPage() {

		server.info("Validating Welcome Page");

		TextInScreen textInScreen = ibm3270Commons.locateText(5, ConstantsTexts.WELCOME_UNIVOCAL_TEXT);

		if (textInScreen != null) {
			server.info(String.format("Text %s found", ConstantsTexts.WELCOME_UNIVOCAL_TEXT));
		} else {
			throw new JidokaFatalException(String.format("Text not %s found", ConstantsTexts.WELCOME_UNIVOCAL_TEXT));
		}
	}

	/**
	 * Action 'Go to NetView' page.
	 * 
	 * @throws JidokaException
	 */
	public void goToNetView() throws JidokaException {

		server.sendScreen("Screenshot before moving to NetView page");

		ibm3270Commons.write("NETVIEW");
		client.pause(1000);
		ibm3270Commons.enter();
		client.pause(1000);
		currentPage = new NetViewPage(client, this, ibm3270Commons).assertIsThisPage();

		server.sendScreen("Moved to NetView page");
	}

	/**
	 * Action 'Change password'.
	 * 
	 * @throws JidokaException
	 */
	public void changePassword() throws JidokaException {

		server.info("Change NetView Password");

		((NetViewPage) currentPage).changeOperatorPassword();
	}

	/**
	 * Action 'Close 3270'.
	 */
	public void close3270() {
		appManager.closeIBM3270(processName, ibm3270Commons.getWindowTitleRegex());
	}

	/**
	 * Action 'End'.
	 */
	public void end() {

		// continue the process, here the robot ends its execution
	}

	/**
	 * Clean up.
	 *
	 * @return the string[]
	 * @throws Exception the exception
	 * @see com.novayre.jidoka.client.api.IRobot#cleanUp()
	 */
	@Override
	public String[] cleanUp() throws Exception {
		appManager.closeIBM3270(processName, ibm3270Commons.getWindowTitleRegex());
		return IRobot.super.cleanUp();
	}

	/**
	 * Manage exception.
	 *
	 * @param action    the action
	 * @param exception the exception
	 * @return the string
	 * @throws Exception the exception
	 */
	@Override
	public String manageException(String action, Exception exception) throws Exception {

		return IRobot.super.manageException(action, exception);
	}

}
