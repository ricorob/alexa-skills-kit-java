/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package capitals;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SsmlOutputSpeech;

import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

/**
 * This sample shows how to create a Lambda function for handling Alexa Skill
 * requests that:
 * 
 * <ul>
 * <li><b>Session State</b>: Handles a multi-turn dialog model.</li>
 * <li><b>Custom slot type</b>: demonstrates using custom slot types to handle a
 * finite set of known values</li>
 * <li><b>SSML</b>: Using SSML tags to control how Alexa renders the
 * text-to-speech</li>
 * </ul>
 * <p>
 * <h2>Examples</h2>
 * <p>
 * <b>Dialog model</b>
 * <p>
 * User: "Alexa, ask Book Options to tell me a knock knock Capital."
 * <p>
 * Alexa: "Knock knock"
 * <p>
 * User: "Who's there?"
 * <p>
 * Alexa: "<phrase>"
 * <p>
 * User: "<phrase> who"
 * <p>
 * Alexa: "<Punchline>"
 */
public class CapitalsSpeechlet implements Speechlet {

	private static final Logger log = LoggerFactory.getLogger(CapitalsSpeechlet.class);

	/**
	 * Session attribute to store the stage the Capital is at.
	 */
	private static final String SESSION_STAGE = "stage";

	/**
	 * Session attribute to store the current Capital ID.
	 */
	private static final String SESSION_Capital_ID = "CapitalId";

	/**
	 * Stage 1 indicates we've already said 'knock knock' and will set up the
	 * Capital next.
	 */
	private static final int FIND_CAPITAL_STAGE = 1;
	/**
	 * Stage 2 indicates we've set up the Capital and will deliver the punchline
	 * next.
	 */
	private static final int SETUP_STAGE = 2;

	/**
	 * ArrayList containing knock knock Capitals.
	 */
	private static final Map<String, Capital> CAPITALS_LIST = new HashMap<String, Capital>();

	static {
		CAPITALS_LIST.put("Afghanistan", new Capital("Afghanistan", "Kabul"));
		CAPITALS_LIST.put("Albania", new Capital("Albania", "Tirana"));
		CAPITALS_LIST.put("Algeria", new Capital("Algeria", "Algiers"));
		CAPITALS_LIST.put("Andorra", new Capital("Andorra", "Andorra la Vella"));
		CAPITALS_LIST.put("Angola", new Capital("Angola", "Luanda"));
		CAPITALS_LIST.put("Antigua and Barbuda", new Capital("Antigua and Barbuda", "Saint John's"));
		CAPITALS_LIST.put("Argentina", new Capital("Argentina", "Buenos Aires"));
		CAPITALS_LIST.put("Armenia", new Capital("Armenia", "Yerevan"));
		CAPITALS_LIST.put("Australia", new Capital("Australia", "Canberra"));
		CAPITALS_LIST.put("Austria", new Capital("Austria", "Vienna"));
		CAPITALS_LIST.put("Azerbaijan", new Capital("Azerbaijan", "Baku"));
		CAPITALS_LIST.put("Bahamas", new Capital("Bahamas", "Nassau"));
		CAPITALS_LIST.put("Bahrain", new Capital("Bahrain", "Manama"));
		CAPITALS_LIST.put("Bangladesh", new Capital("Bangladesh", "Dhaka"));
		CAPITALS_LIST.put("Barbados", new Capital("Barbados", "Bridgetown"));
		CAPITALS_LIST.put("Belarus", new Capital("Belarus", "Minsk"));
		CAPITALS_LIST.put("Belgium", new Capital("Belgium", "Brussels"));
		CAPITALS_LIST.put("Belize", new Capital("Belize", "Belmopan"));
		CAPITALS_LIST.put("Benin", new Capital("Benin", "Porto-Novo"));
		CAPITALS_LIST.put("Bhutan", new Capital("Bhutan", "Thimphu"));
		CAPITALS_LIST.put("Bolivia", new Capital("Bolivia", "La Paz"));
		CAPITALS_LIST.put("Bosnia and Herzegovina", new Capital("Bosnia and Herzegovina", "Sarajevo"));
		CAPITALS_LIST.put("Botswana", new Capital("Botswana", "Gaborone"));
		CAPITALS_LIST.put("Brazil", new Capital("Brazil", "Brasilia"));
		CAPITALS_LIST.put("Brunei", new Capital("Brunei", "Bandar Seri Begawan"));
		CAPITALS_LIST.put("Bulgaria", new Capital("Bulgaria", "Sofia"));
		CAPITALS_LIST.put("Burkina Faso", new Capital("Burkina Faso", "Ouagadougou"));
		CAPITALS_LIST.put("Burundi", new Capital("Burundi", "Bujumbura"));
		CAPITALS_LIST.put("Cabo Verde", new Capital("Cabo Verde", "Praia"));
		CAPITALS_LIST.put("Cambodia", new Capital("Cambodia", "Phnom Penh"));
		CAPITALS_LIST.put("Cameroon", new Capital("Cameroon", "Yaounde"));
		CAPITALS_LIST.put("Canada", new Capital("Canada", "Ottawa"));
		CAPITALS_LIST.put("Central African Republic", new Capital("Central African Republic", "Bangui"));
		CAPITALS_LIST.put("Chad", new Capital("Chad", "N'Djamena"));
		CAPITALS_LIST.put("Chile", new Capital("Chile", "Santiago"));
		CAPITALS_LIST.put("China", new Capital("China", "Beijing"));
		CAPITALS_LIST.put("Colombia", new Capital("Colombia", "Bogotá"));
		CAPITALS_LIST.put("Comoros", new Capital("Comoros", "Moroni"));
		CAPITALS_LIST.put("Democratic Republic of the Congo",new Capital("Democratic Republic of the Congo", "Kinshasa"));
		CAPITALS_LIST.put("Republic of the Congo", new Capital("Republic of the Congo", "Brazzaville"));
		CAPITALS_LIST.put("Costa Rica", new Capital("Costa Rica", "San Jose"));
		CAPITALS_LIST.put("Cote d'Ivoire", new Capital("Cote d'Ivoire", "Yamoussoukro"));
		CAPITALS_LIST.put("Croatia", new Capital("Croatia", "Zagreb"));
		CAPITALS_LIST.put("Cuba", new Capital("Cuba", "Havana"));
		CAPITALS_LIST.put("Cyprus", new Capital("Cyprus", "Nicosia"));
		CAPITALS_LIST.put("Czech Republic", new Capital("Czech Republic", "Prague"));
		CAPITALS_LIST.put("Denmark", new Capital("Denmark", "Copenhagen"));
		CAPITALS_LIST.put("Djibouti", new Capital("Djibouti", "Djibouti"));
		CAPITALS_LIST.put("Dominica", new Capital("Dominica", "Roseau"));
		CAPITALS_LIST.put("Dominican Republic", new Capital("Dominican Republic", "Santo Domingo"));
		CAPITALS_LIST.put("Ecuador", new Capital("Ecuador", "Quito"));
		CAPITALS_LIST.put("Egypt", new Capital("Egypt", "Cairo"));
		CAPITALS_LIST.put("El Salvador", new Capital("El Salvador", "San Salvador"));
		CAPITALS_LIST.put("Equatorial Guinea", new Capital("Equatorial Guinea", "Malabo"));
		CAPITALS_LIST.put("Eritrea", new Capital("Eritrea", "Asmara"));
		CAPITALS_LIST.put("Estonia", new Capital("Estonia", "Tallinn"));
		CAPITALS_LIST.put("Ethiopia", new Capital("Ethiopia", "Addis Ababa"));
		CAPITALS_LIST.put("Fiji", new Capital("Fiji", "Suva"));
		CAPITALS_LIST.put("Finland", new Capital("Finland", "Helsinki"));
		CAPITALS_LIST.put("France", new Capital("France", "Paris"));
		CAPITALS_LIST.put("Gabon", new Capital("Gabon", "Libreville"));
		CAPITALS_LIST.put("Gambia", new Capital("Gambia", "Banjul"));
		CAPITALS_LIST.put("Georgia", new Capital("Georgia", "Tbilisi"));
		CAPITALS_LIST.put("Germany", new Capital("Germany", "Berlin"));
		CAPITALS_LIST.put("Ghana", new Capital("Ghana", "Accra"));
		CAPITALS_LIST.put("Greece", new Capital("Greece", "Athens"));
		CAPITALS_LIST.put("Grenada", new Capital("Grenada", "Saint George's"));
		CAPITALS_LIST.put("Guatemala", new Capital("Guatemala", "Guatemala City"));
		CAPITALS_LIST.put("Guinea", new Capital("Guinea", "Conakry"));
		CAPITALS_LIST.put("Guinea-Bissau", new Capital("Guinea-Bissau", "Bissau"));
		CAPITALS_LIST.put("Guyana", new Capital("Guyana", "Georgetown"));
		CAPITALS_LIST.put("Haiti", new Capital("Haiti", "Port-au-Prince"));
		CAPITALS_LIST.put("Honduras", new Capital("Honduras", "Tegucigalpa"));
		CAPITALS_LIST.put("Hungary", new Capital("Hungary", "Budapest"));
		CAPITALS_LIST.put("Iceland", new Capital("Iceland", "Reykjavik"));
		CAPITALS_LIST.put("India", new Capital("India", "New Delhi"));
		CAPITALS_LIST.put("Indonesia", new Capital("Indonesia", "Jakarta"));
		CAPITALS_LIST.put("Iran", new Capital("Iran", "Tehran"));
		CAPITALS_LIST.put("Iraq", new Capital("Iraq", "Baghdad"));
		CAPITALS_LIST.put("Ireland", new Capital("Ireland", "Dublin"));
		CAPITALS_LIST.put("Israel", new Capital("Israel", "Jerusalem"));
		CAPITALS_LIST.put("Italy", new Capital("Italy", "Rome"));
		CAPITALS_LIST.put("Jamaica", new Capital("Jamaica", "Kingston"));
		CAPITALS_LIST.put("Japan", new Capital("Japan", "Tokyo"));
		CAPITALS_LIST.put("Jordan", new Capital("Jordan", "Amman"));
		CAPITALS_LIST.put("Kazakhstan", new Capital("Kazakhstan", "Astana"));
		CAPITALS_LIST.put("Kenya", new Capital("Kenya", "Nairobi"));
		CAPITALS_LIST.put("Kiribati", new Capital("Kiribati", "South Tarawa"));
		CAPITALS_LIST.put("Kosovo", new Capital("Kosovo", "Pristina"));
		CAPITALS_LIST.put("Kuwait", new Capital("Kuwait", "Kuwait City"));
		CAPITALS_LIST.put("Kyrgyzstan", new Capital("Kyrgyzstan", "Bishkek"));
		CAPITALS_LIST.put("Laos", new Capital("Laos", "Vientiane"));
		CAPITALS_LIST.put("Latvia", new Capital("Latvia", "Riga"));
		CAPITALS_LIST.put("Lebanon", new Capital("Lebanon", "Beirut"));
		CAPITALS_LIST.put("Lesotho", new Capital("Lesotho", "Maseru"));
		CAPITALS_LIST.put("Liberia", new Capital("Liberia", "Monrovia"));
		CAPITALS_LIST.put("Libya", new Capital("Libya", "Tripoli"));
		CAPITALS_LIST.put("Liechtenstein", new Capital("Liechtenstein", "Vaduz"));
		CAPITALS_LIST.put("Lithuania", new Capital("Lithuania", "Vilnius"));
		CAPITALS_LIST.put("Luxembourg", new Capital("Luxembourg", "Luxembourg"));
		CAPITALS_LIST.put("Macedonia", new Capital("Macedonia", "Skopje"));
		CAPITALS_LIST.put("Madagascar", new Capital("Madagascar", "Antananarivo"));
		CAPITALS_LIST.put("Malawi", new Capital("Malawi", "Lilongwe"));
		CAPITALS_LIST.put("Malaysia", new Capital("Malaysia", "Kuala Lumpur"));
		CAPITALS_LIST.put("Maldives", new Capital("Maldives", "Male"));
		CAPITALS_LIST.put("Mali", new Capital("Mali", "Bamako"));
		CAPITALS_LIST.put("Malta", new Capital("Malta", "Valletta"));
		CAPITALS_LIST.put("Marshall Islands", new Capital("Marshall Islands", "Majuro"));
		CAPITALS_LIST.put("Mauritania", new Capital("Mauritania", "Nouakchott"));
		CAPITALS_LIST.put("Mauritius", new Capital("Mauritius", "Port Louis"));
		CAPITALS_LIST.put("Mexico", new Capital("Mexico", "Mexico City"));
		CAPITALS_LIST.put("Micronesia", new Capital("Micronesia", "Palikir"));
		CAPITALS_LIST.put("Moldova", new Capital("Moldova", "Chisinau"));
		CAPITALS_LIST.put("Monaco", new Capital("Monaco", "Monaco"));
		CAPITALS_LIST.put("Mongolia", new Capital("Mongolia", "Ulaanbaatar"));
		CAPITALS_LIST.put("Montenegro", new Capital("Montenegro", "Podgorica"));
		CAPITALS_LIST.put("Morocco", new Capital("Morocco", "Rabat"));
		CAPITALS_LIST.put("Mozambique", new Capital("Mozambique", "Maputo"));
		CAPITALS_LIST.put("Myanmar (Burma)", new Capital("Myanmar (Burma)", "Naypyidaw"));
		CAPITALS_LIST.put("Namibia", new Capital("Namibia", "Windhoek"));
		CAPITALS_LIST.put("Nauru", new Capital("Nauru", "Yaren District"));
		CAPITALS_LIST.put("Nepal", new Capital("Nepal", "Kathmandu"));
		CAPITALS_LIST.put("Netherlands", new Capital("Netherlands", "Amsterdam"));
		CAPITALS_LIST.put("New Zealand", new Capital("New Zealand", "Wellington"));
		CAPITALS_LIST.put("Nicaragua", new Capital("Nicaragua", "Managua"));
		CAPITALS_LIST.put("Niger", new Capital("Niger", "Niamey"));
		CAPITALS_LIST.put("Nigeria", new Capital("Nigeria", "Abuja"));
		CAPITALS_LIST.put("North Korea", new Capital("North Korea", "Pyongyang"));
		CAPITALS_LIST.put("Norway", new Capital("Norway", "Oslo"));
		CAPITALS_LIST.put("Oman", new Capital("Oman", "Muscat"));
		CAPITALS_LIST.put("Pakistan", new Capital("Pakistan", "Islamabad"));
		CAPITALS_LIST.put("Palau", new Capital("Palau", "Ngerulmud"));
		CAPITALS_LIST.put("Palestine", new Capital("Palestine", "Ramallah"));
		CAPITALS_LIST.put("Panama", new Capital("Panama", "Panama City"));
		CAPITALS_LIST.put("Papua New Guinea", new Capital("Papua New Guinea", "Port Moresby"));
		CAPITALS_LIST.put("Paraguay", new Capital("Paraguay", "Asunción"));
		CAPITALS_LIST.put("Peru", new Capital("Peru", "Lima"));
		CAPITALS_LIST.put("Philippines", new Capital("Philippines", "Manila"));
		CAPITALS_LIST.put("Poland", new Capital("Poland", "Warsaw"));
		CAPITALS_LIST.put("Portugal", new Capital("Portugal", "Lisbon"));
		CAPITALS_LIST.put("Qatar", new Capital("Qatar", "Doha"));
		CAPITALS_LIST.put("Romania", new Capital("Romania", "Bucharest"));
		CAPITALS_LIST.put("Russia", new Capital("Russia", "Moscow"));
		CAPITALS_LIST.put("Rwanda", new Capital("Rwanda", "Kigali"));
		CAPITALS_LIST.put("Saint Kitts and Nevis", new Capital("Saint Kitts and Nevis", "Basseterre"));
		CAPITALS_LIST.put("Saint Lucia", new Capital("Saint Lucia", "Castries"));
		CAPITALS_LIST.put("Saint Vincent and the Grenadines",new Capital("Saint Vincent and the Grenadines", "Kingstown"));
		CAPITALS_LIST.put("Samoa", new Capital("Samoa", "Apia"));
		CAPITALS_LIST.put("San Marino", new Capital("San Marino", "San Marino"));
		CAPITALS_LIST.put("Sao Tome and Principe", new Capital("Sao Tome and Principe", "São Tomé"));
		CAPITALS_LIST.put("Saudi Arabia", new Capital("Saudi Arabia", "Riyadh"));
		CAPITALS_LIST.put("Senegal", new Capital("Senegal", "Dakar"));
		CAPITALS_LIST.put("Serbia", new Capital("Serbia", "Belgrade"));
		CAPITALS_LIST.put("Seychelles", new Capital("Seychelles", "Victoria"));
		CAPITALS_LIST.put("Sierra Leone", new Capital("Sierra Leone", "Freetown"));
		CAPITALS_LIST.put("Singapore", new Capital("Singapore", "Singapore"));
		CAPITALS_LIST.put("Slovakia", new Capital("Slovakia", "Bratislava"));
		CAPITALS_LIST.put("Slovenia", new Capital("Slovenia", "Ljubljana"));
		CAPITALS_LIST.put("Solomon Islands", new Capital("Solomon Islands", "Honiara"));
		CAPITALS_LIST.put("Somalia", new Capital("Somalia", "Mogadishu"));
		CAPITALS_LIST.put("South Africa", new Capital("South Africa", "Pretoria"));
		CAPITALS_LIST.put("South Korea", new Capital("South Korea", "Seoul"));
		CAPITALS_LIST.put("South Sudan", new Capital("South Sudan", "Juba"));
		CAPITALS_LIST.put("Spain", new Capital("Spain", "Madrid"));
		CAPITALS_LIST.put("Sri Lanka", new Capital("Sri Lanka", "Sri Jayawardenepura Kotte"));
		CAPITALS_LIST.put("Sudan", new Capital("Sudan", "Khartoum"));
		CAPITALS_LIST.put("Suriname", new Capital("Suriname", "Paramaribo"));
		CAPITALS_LIST.put("Swaziland", new Capital("Swaziland", "Mbabane"));
		CAPITALS_LIST.put("Sweden", new Capital("Sweden", "Stockholm"));
		CAPITALS_LIST.put("Switzerland", new Capital("Switzerland", "Bern"));
		CAPITALS_LIST.put("Syria", new Capital("Syria", "Damascus"));
		CAPITALS_LIST.put("Taiwan", new Capital("Taiwan", "Taipei"));
		CAPITALS_LIST.put("Tajikistan", new Capital("Tajikistan", "Dushanbe"));
		CAPITALS_LIST.put("Tanzania", new Capital("Tanzania", "Dodoma"));
		CAPITALS_LIST.put("Thailand", new Capital("Thailand", "Bangkok"));
		CAPITALS_LIST.put("Timor-Leste", new Capital("Timor-Leste", "Dili"));
		CAPITALS_LIST.put("Togo", new Capital("Togo", "Lomé"));
		CAPITALS_LIST.put("Tonga", new Capital("Tonga", "Nukualofa"));
		CAPITALS_LIST.put("Trinidad and Tobago", new Capital("Trinidad and Tobago", "Port of Spain"));
		CAPITALS_LIST.put("Tunisia", new Capital("Tunisia", "Tunis"));
		CAPITALS_LIST.put("Turkey", new Capital("Turkey", "Ankara"));
		CAPITALS_LIST.put("Turkmenistan", new Capital("Turkmenistan", "Ashgabat"));
		CAPITALS_LIST.put("Tuvalu", new Capital("Tuvalu", "Funafuti"));
		CAPITALS_LIST.put("Uganda", new Capital("Uganda", "Kampala"));
		CAPITALS_LIST.put("Ukraine", new Capital("Ukraine", "Kyiv"));
		CAPITALS_LIST.put("United Arab Emirates", new Capital("United Arab Emirates", "Abu Dhabi"));
		CAPITALS_LIST.put("United Kingdom", new Capital("United Kingdom", "London"));
		CAPITALS_LIST.put("United States of America", new Capital("United States of America", "Washington D.C."));
		CAPITALS_LIST.put("Uruguay", new Capital("Uruguay", "Montevideo"));
		CAPITALS_LIST.put("Uzbekistan", new Capital("Uzbekistan", "Tashkent"));
		CAPITALS_LIST.put("Vanuatu", new Capital("Vanuatu", "Port Vila"));
		CAPITALS_LIST.put("Vatican City (Holy See)", new Capital("Vatican City (Holy See)", "Vatican City"));
		CAPITALS_LIST.put("Venezuela", new Capital("Venezuela", "Caracas"));
		CAPITALS_LIST.put("Vietnam", new Capital("Vietnam", "Hanoi"));
		CAPITALS_LIST.put("Yemen", new Capital("Yemen", "Sana'a"));
		CAPITALS_LIST.put("Zambia", new Capital("Zambia", "Lusaka"));
		CAPITALS_LIST.put("Zimbabwe", new Capital("Zimbabwe", "Harare"));
	}

	@Override
	public void onSessionStarted(final SessionStartedRequest request, final Session session) throws SpeechletException {
		log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		// any initialization logic goes here
	}

	@Override
	public SpeechletResponse onLaunch(final LaunchRequest request, final Session session) throws SpeechletException {
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		return handleTellMeACapitalIntent(session);
	}

	@Override
	public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {
		log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		Intent intent = request.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;

		if ("TellMeTheCapitalIntent".equals(intentName)) {
			return handleTellMeACapitalIntent(session);
		} else if ("SetupCapitalIntent".equals(intentName)) {
			return handleSetupCapitalIntent(intent, session);
		} else if ("AMAZON.HelpIntent".equals(intentName)) {
			String speechOutput = "";
			int stage = -1;
			if (session.getAttributes().containsKey(SESSION_STAGE)) {
				stage = (Integer) session.getAttribute(SESSION_STAGE);
			}
			switch (stage) {
			case 0:
				speechOutput = "Capitals are related to a country. " + "To find a Capital, just ask by saying tell me a"
						+ " Capital, or you can say exit.";
				break;
			case 1:
				speechOutput = "You can ask, tell me the capital, or you can say exit.";
				break;
			case 2:
				speechOutput = "You can ask, WHAT TO PUT HERE?, or you can say exit.";
				break;
			default:
				speechOutput = "Capitals are related to a country. " + "To find a Capital, just ask by saying tell me a"
						+ " Capital, or you can say exit.";
			}

			String repromptText = speechOutput;
			return newAskResponse(speechOutput, false, repromptText, false);
		} else if ("AMAZON.StopIntent".equals(intentName)) {
			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText("Goodbye");

			return SpeechletResponse.newTellResponse(outputSpeech);
		} else if ("AMAZON.CancelIntent".equals(intentName)) {
			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText("Goodbye");

			return SpeechletResponse.newTellResponse(outputSpeech);
		} else {
			throw new SpeechletException("Invalid Intent");
		}
	}

	@Override
	public void onSessionEnded(final SessionEndedRequest request, final Session session) throws SpeechletException {
		log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		// any session cleanup logic would go here
	}

	/**
	 * Selects a Capital randomly and starts it off by saying "Knock knock".
	 *
	 * @param session
	 *            the session object
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse handleTellMeACapitalIntent(final Session session) {
		String speechOutput = "";

		// Reprompt speech will be triggered if the user doesn't respond.
		String repromptText = "You can ask, find a capital";

		// / Select a random Capital and store it in the session variables
		int CapitalID = (int) Math.floor(Math.random() * CAPITALS_LIST.size());

		// The stage variable tracks the phase of the dialogue.
		// When this function completes, it will be on stage 1.
		session.setAttribute(SESSION_STAGE, SETUP_STAGE);
		session.setAttribute(SESSION_Capital_ID, CapitalID);
		speechOutput = "What Country's capital are you lookimg for?";

		// Create the Simple card content.
		SimpleCard card = new SimpleCard();
		card.setTitle("Capital Finder");
		card.setContent(speechOutput);

		SpeechletResponse response = newAskResponse(speechOutput, false, repromptText, false);
		response.setCard(card);
		return response;
	}

	/**
	 * Delivers the punchline of the Capital after the user responds to the
	 * setup.
	 *
	 * @param session
	 *            the session object
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse handleSetupCapitalIntent(final Intent intent, final Session session) {
		String speechOutput = "", repromptText = "";
		// Find the capital
		Map<String, Slot> slots = intent.getSlots();
		// Get the color slot from the list of slots.
		Slot countrySlot = slots.get("Country");

		// Create the Simple card content.
		SimpleCard card = new SimpleCard();
		card.setTitle("Capital Finder");

		if (session.getAttributes().containsKey(SESSION_STAGE)) {
			if ((Integer) session.getAttribute(SESSION_STAGE) == SETUP_STAGE && countrySlot != null) {
				// Store the user's favorite color in the Session and create
				// response.
				String countryId = countrySlot.getValue();
				System.out.println("capitalValue");
				// int CapitalID = (Integer)
				// session.getAttribute(SESSION_Capital_ID);
				speechOutput = CAPITALS_LIST.get(countryId).capital;
				card.setContent(CAPITALS_LIST.get(countryId).capital);

				// Create the ssml text output
				SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
				outputSpeech.setSsml("<speak>" + "The capital of "+ countryId + " is " + speechOutput + "</speak>");

				// If the Capital completes successfully, this function will end
				// the active session
				return SpeechletResponse.newTellResponse(outputSpeech, card);
			} else {
				session.setAttribute(SESSION_STAGE, FIND_CAPITAL_STAGE);
				speechOutput = "That's not a valid country <break time=\"0.3s\" />";
				repromptText = "You can say find a capital.";

				card.setContent("That's not a valid country.");

				// Create the ssml text output
				SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
				outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");
				PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
				repromptOutputSpeech.setText(repromptText);
				Reprompt repromptSpeech = new Reprompt();
				repromptSpeech.setOutputSpeech(repromptOutputSpeech);

				// If the Capital has to be restarted, then keep the session
				// alive
				return SpeechletResponse.newAskResponse(outputSpeech, repromptSpeech, card);
			}
		} else {
			speechOutput = "Sorry, I couldn't correctly retrieve the Capital. You can say, tell me a Capital";
			repromptText = "You can say, tell me a Capital";
			card.setContent(speechOutput);
			SpeechletResponse response = newAskResponse(speechOutput, false, repromptText, false);
			response.setCard(card);
			return response;
		}
	}

	/**
	 * Wrapper for creating the Ask response from the input strings.
	 * 
	 * @param stringOutput
	 *            the output to be spoken
	 * @param isOutputSsml
	 *            whether the output text is of type SSML
	 * @param repromptText
	 *            the reprompt for if the user doesn't reply or is
	 *            misunderstood.
	 * @param isRepromptSsml
	 *            whether the reprompt text is of type SSML
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newAskResponse(String stringOutput, boolean isOutputSsml, String repromptText,
			boolean isRepromptSsml) {
		OutputSpeech outputSpeech, repromptOutputSpeech;
		if (isOutputSsml) {
			outputSpeech = new SsmlOutputSpeech();
			((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
		} else {
			outputSpeech = new PlainTextOutputSpeech();
			((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
		}

		if (isRepromptSsml) {
			repromptOutputSpeech = new SsmlOutputSpeech();
			((SsmlOutputSpeech) repromptOutputSpeech).setSsml(repromptText);
		} else {
			repromptOutputSpeech = new PlainTextOutputSpeech();
			((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
		}
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);
		return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
	}

	private static class Capital {

		private final String country;
		private final String capital;

		Capital(String country, String capital) {
			this.country = country;
			this.capital = capital;
		}
	}
}
