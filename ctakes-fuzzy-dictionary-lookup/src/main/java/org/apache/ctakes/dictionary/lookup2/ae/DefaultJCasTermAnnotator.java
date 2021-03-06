/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ctakes.dictionary.lookup2.ae;

import org.apache.ctakes.core.config.ConfigParameterConstants;
import org.apache.ctakes.core.pipeline.PipeBitInfo;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.core.util.collection.CollectionMap;
import org.apache.ctakes.dictionary.lookup2.dictionary.RareWordDictionary;
import org.apache.ctakes.dictionary.lookup2.term.RareWordTerm;
import org.apache.ctakes.dictionary.lookup2.textspan.DefaultTextSpan;
import org.apache.ctakes.dictionary.lookup2.textspan.TextSpan;
import org.apache.ctakes.dictionary.lookup2.util.FastLookupToken;
import org.apache.ctakes.typesystem.type.heideltime.Timex3;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.ihtsdo.otf.spellcheck.service.RemoveAccents;
import org.ihtsdo.otf.spellcheck.service.SpellCheckService;
import org.apache.ctakes.dictionary.lookup2.textspan.TextSpan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A direct string match using phrase permutations Author: SPF Affiliation:
 * CHIP-NLP Date: 11/19/13
 */
@PipeBitInfo(name = "Dictionary Lookup (Default)", description = "Annotates clinically-relevant terms.  Terms must match dictionary entries exactly.", dependencies = {
		PipeBitInfo.TypeProduct.SENTENCE,
		PipeBitInfo.TypeProduct.BASE_TOKEN }, products = PipeBitInfo.TypeProduct.IDENTIFIED_ANNOTATION)
public class DefaultJCasTermAnnotator extends AbstractJCasTermAnnotator {

	final static private Logger LOGGER = Logger.getLogger("DefaultJCasTermAnnotator");
	static SpellCheckService spellchecker;
	static SpellCheckService spellchecker_Sent;
	final private float accuracy_oneToken = 0.7f;
	final static private float accuracy_sentence = 0.83f;

	List<String> score_list = Arrays.asList("niss", "nih", "nhiss", "nishss", "nissh", "nihss", "mrankin", "rankin",
			"aspects", "mrs", "escala nihss", "excala de rankin modificada", "aspects score", "mRs", "mrankinscale");
	static List<String> score_after = Arrays.asList("h", "lalta", "puntos", "a", "de", "total", "estimado", "actual",
			"al", "alta", "b", "c", "resulta", "entre", "previo", "previ", "historico", "basal", "es", "score", "las");

	public DefaultJCasTermAnnotator() {
		// TODO Auto-generated constructor stub
		try {

			LOGGER.info("Loading SpellChecker Dictionaries");
			spellchecker = new SpellCheckService();
			spellchecker_Sent = new SpellCheckService();
			if (ConfigParameterConstants.lemmaForm)
				spellchecker.loadDirectoryOfDictionaries(
						"org/apache/ctakes/examples/dictionary/lookup/spellchecker_lemma/");
			else {
				InputStream descriptorStream = FileLocator
						.getAsStream("org/apache/ctakes/examples/dictionary/lookup/spellchecker/lexicon/lexicon.txt");
				spellchecker.loadDirectoryOfDictionaries(descriptorStream, "lexicon");
				descriptorStream = FileLocator
						.getAsStream("org/apache/ctakes/examples/dictionary/lookup/spellchecker/dic/dic.txt");
				spellchecker_Sent.loadDirectoryOfDictionaries(descriptorStream, "dic");
			}

		} catch (IOException e) {

			LOGGER.error("Error in Spell Checker");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void findTerms(final RareWordDictionary dictionary, final List<FastLookupToken> allTokens,
			final List<Integer> lookupTokenIndices,
			final CollectionMap<TextSpan, Long, ? extends Collection<Long>> termsFromDictionary) {
		Collection<RareWordTerm> rareWordHits;
//		final List<FastLookupToken> allTokens_original = new ArrayList<>();
//		final List<Integer> lookupTokenIndices_original = new ArrayList<>();
//
//		for (FastLookupToken tokens : allTokens)
//			allTokens_original.add(tokens);
//
//		for (Integer tokens : lookupTokenIndices)
//			lookupTokenIndices_original.add(tokens);

		final Map<TextSpan, Long> placeValue = new HashMap<>();

//		int intTemp = lookupTokenIndices.get(lookupTokenIndices.size() - 1);
//		try {
//			if (allTokens.get(intTemp).getText().equals(".")) {
//				allTokens.remove(intTemp);
//				lookupTokenIndices.remove(intTemp);
//			}
//		} catch (Exception e) {
//			System.out.println(intTemp);
//		}

		for (Integer lookupTokenIndex : lookupTokenIndices) {

			final FastLookupToken lookupToken = allTokens.get(lookupTokenIndex);
			String tempText = lookupToken.getText();
//			LOGGER.info("New Words:     " + tempText);
			RemoveAccents rc = new RemoveAccents();
			tempText = rc.removeAccents(tempText);
//			LOGGER.info("Removed: " + tempText);

			if (tempText.toLowerCase().startsWith("arterias")) {
				int xxx = 0;
			}

//			if (tempText.equalsIgnoreCase("habitual")) {
//				System.out.println("Token");
//			}
//
//			if (tempText.equalsIgnoreCase("dismetrias")) {
//				System.out.println("Token");
//			}

			Map<String, List<String>> suggestions = spellchecker
					.checkWordsReturnErrorSuggestions(Arrays.asList(tempText), accuracy_oneToken);
			Map<String, List<String>> suggestions_sent = spellchecker_Sent
					.checkWordsReturnErrorSuggestions(Arrays.asList(tempText), accuracy_sentence);

			String temp = "";
			try {
//  		System.out.println(suggestions.entrySet().stream().count());
				if (suggestions.entrySet().stream().count() >= 1
						&& !suggestions.entrySet().stream().findFirst().get().getValue().isEmpty())
					temp = suggestions.entrySet().stream().findFirst().get().getValue().get(0);
				else {
					temp = tempText;
				}
			} catch (Exception e) {
				System.out.println(lookupToken.getText());
			}

			String temp_sent = "";
			try {
//  		System.out.println(suggestions.entrySet().stream().count());
				if (suggestions.entrySet().stream().count() >= 1
						&& !suggestions.entrySet().stream().findFirst().get().getValue().isEmpty())
					temp_sent = suggestions.entrySet().stream().findFirst().get().getValue().get(0);
				else {
					temp_sent = tempText;
				}
			} catch (Exception e) {
				System.out.println(lookupToken.getText());
			}

//			LOGGER.info("dic: " + temp);

//         rareWordHits = dictionary.getRareWordHits( lookupToken );

			String removedTemp = punctuationRemover(temp);
//			String acm_word = "l'acm";
//			
//			if (temp.equalsIgnoreCase(acm_word)) {
//				temp = "acm";
//			}
//			LOGGER.info("removedTemp: " + removedTemp);
			if (score_list.contains(removedTemp))
				rareWordHits = dictionary.getRareWordHits(removedTemp);
			else
				rareWordHits = dictionary.getRareWordHits(temp);

			if (rareWordHits == null) {
				rareWordHits = dictionary.getRareWordHits(temp_sent);
			}

			if (rareWordHits == null || rareWordHits.isEmpty()) {
				continue;
			}
			for (RareWordTerm rareWordHit : rareWordHits) {
//				LOGGER.info("rareWordHit: " + rareWordHit.getText());

				if (rareWordHit.getText().length() < _minimumLookupSpan) {
					continue;
				}
				if (rareWordHit.getText().length() < 3) {
					if (rareWordHit.getText().contentEquals(lookupToken.getText())) {
						placeValue.put(lookupToken.getTextSpan(), rareWordHit.getCuiCode());
					}
					continue;

				}
				if (rareWordHit.getTokenCount() == 1) {
					// Single word term, add and move on
					String temp_one = "";
					try {
//		  		System.out.println(suggestions.entrySet().stream().count());
						if (suggestions.entrySet().stream().count() >= 1
								&& !suggestions.entrySet().stream().findFirst().get().getValue().isEmpty())
							temp_one = suggestions.entrySet().stream().findFirst().get().getValue().get(0);
						else {
							temp_one = tempText;
//							if (temp_one.equalsIgnoreCase(acm_word)) {
//								temp_one = "acm";
//							}
						}
					} catch (Exception e) {
						System.out.println(lookupToken.getText());
					}
					if (rareWordHit.getRareWord().contentEquals(temp_one))
//						LOGGER.info("OK temo_one: " + rareWordHit.getText());
//						if (tempText.equalsIgnoreCase(acm_word)){
//							placeValue.put(lookupToken.getTexTSpanACM(), rareWordHit.getCuiCode());
//						}else {
//							placeValue.put(lookupToken.getTextSpan(), rareWordHit.getCuiCode());
//						}
						placeValue.put(lookupToken.getTextSpan(), rareWordHit.getCuiCode());

					continue;
				}
				final int termStartIndex = lookupTokenIndex - rareWordHit.getRareWordIndex();
//				if (termStartIndex < 0 || termStartIndex + rareWordHit.getTokenCount() > allTokens.size()) {
//					// term will extend beyond window
//					continue;
//				}

				int endIndex = rareWordHit.getTokenCount() < allTokens.size() ? rareWordHit.getTokenCount()
						: allTokens.size();
//            final int termEndIndex = termStartIndex + rareWordHit.getTokenCount() - 1;
				final int termEndIndex = termStartIndex + endIndex - 1;
				if (score_list.contains(removedTemp) || termEndIndex < allTokens.size()) {
					int Score = isTermMatchfuzzy(temp, rareWordHit, allTokens, termStartIndex, endIndex);
					if (score_list.contains(removedTemp) && Score != 0) {

						final int spanStart = allTokens.get(termStartIndex).getStart();
//						System.out.println(spanStart);
						final int spanEnd = allTokens.get(termStartIndex + Score - 1).getEnd();
//						System.out.println(spanEnd);
						placeValue.put(new DefaultTextSpan(spanStart, spanEnd), rareWordHit.getCuiCode());

					} else {
						if (isTermMatch(temp, rareWordHit, allTokens, termStartIndex, termEndIndex)) {
							try {

								final int spanStart = allTokens.get(termStartIndex).getStart();
//							System.out.println(spanStart);
								final int spanEnd = allTokens.get(termEndIndex).getEnd();
//							System.out.println(spanEnd);
								placeValue.put(new DefaultTextSpan(spanStart, spanEnd), rareWordHit.getCuiCode());

							} catch (Exception e) {
								System.out.println("error " + temp);
							}

						}
					}

				}

			}
		}

		// Keeping the longest span for the same categories.
		Map<TextSpan, Long> placeValue_new = new HashMap<>(placeValue);
		for (Map.Entry ent1 : placeValue.entrySet()) {

			Map<TextSpan, Long> placeValue_copy = new HashMap<>(placeValue_new);
			for (Map.Entry ent2 : placeValue_copy.entrySet()) {
				if (ent1.getValue().equals(ent2.getValue())) {
					TextSpan e1 = (TextSpan) ent1.getKey();
					TextSpan e2 = (TextSpan) ent2.getKey();
					if (((e1.getStart() == e2.getStart()) && (e1.getEnd() < e2.getEnd()))
							|| ((e1.getStart() > e2.getStart()) && (e1.getEnd() < e2.getEnd()))
							|| ((e1.getStart() > e2.getStart()) && (e1.getEnd() == e2.getEnd()))) {
						placeValue_new.remove((TextSpan) ent1.getKey(), (Long) ent1.getValue());
						break;
					}
//					if ((e1.getStart() == e2.getStart()) && (e1.getEnd() < e2.getEnd()))  {
//						placeValue_new.remove((TextSpan) ent1.getKey(), (Long) ent1.getValue());
//							break;					
//					}

				}
			}
		}
		for (Map.Entry ent : placeValue_new.entrySet()) {
			termsFromDictionary.placeValue((TextSpan) ent.getKey(), (Long) ent.getValue());
		}
	}

	/**
	 * Hopefully the jit will inline this method
	 *
	 * @param rareWordHit    rare word term to check for match
	 * @param allTokens      all tokens in a window
	 * @param termStartIndex index of first token in allTokens to check
	 * @param termEndIndex   index of last token in allTokens to check
	 * @return true if the rare word term exists in allTokens within the given
	 *         indices
	 */
//   public static boolean isTermMatch( final RareWordTerm rareWordHit, final List<FastLookupToken> allTokens,
//                                      final int termStartIndex, final int termEndIndex ) {
//      final String[] hitTokens = rareWordHit.getTokens();
//      int hit = 0;
//      for ( int i = termStartIndex; i < termEndIndex + 1; i++ ) {
//         if ( hitTokens[ hit ].equals( allTokens.get( i ).getText() )
//              || hitTokens[ hit ].equals( allTokens.get( i ).getVariant() ) ) {
//            // the normal token or variant matched, move to the next token
//            hit++;
//            continue;
//         }
//         // the token normal didn't match and there is no matching variant
//         return false;
//      }
//      // some combination of token and variant matched
//      return true;
//   }

	public static String punctuationRemover(final String word) {
		String removedWord = word.replaceAll("[^a-zA-Z ]", "");

		return removedWord.trim();
	}

	public static int isTermMatchfuzzy(final String temp, final RareWordTerm rareWordHit,
			final List<FastLookupToken> allTokens, final int termStartIndex, final int endIndex) {
		String word = temp + " ";
		for (Integer i = termStartIndex + 1; i < allTokens.size(); i++) {
			String pureWord = punctuationRemover(allTokens.get(i).getText());
			char firstchar = (pureWord.isEmpty()) ? (char) 0 : pureWord.charAt(0);
			if (Character.isLetter(firstchar) || allTokens.get(i).getText().equalsIgnoreCase("\t")) {
				if (!score_after.contains(pureWord)) {
					break;
				}

			}
			word += allTokens.get(i).getText() + " ";
		}

		word = word.trim();
		if (word.endsWith(" a")) {
			word = word.substring(0, word.lastIndexOf(" a"));
		}
//		Integer count = word.length() - 1;
//		if (!myList.contains(allTokens.get(allTokens.size() - 1).getText())) {
//			for (int i = count; i >= 0; i--) {
//				Character c = word.charAt(i);
//				if (!Character.isDigit(word.charAt(i))) {
//					word = word.substring(0, i);
//				} else {
//					break;
//				}
//			}
//		}
		Integer count = word.length() - 1;
		for (int i = count; i >= 0; i--) {
			Character c = word.charAt(i);
			if (isPunctuation(c)) {
//		if (!Character.isDigit(word.charAt(i))) {
				word = word.substring(0, i);
			} else {
				break;
			}
		}
		word = word.trim();

		return word.split(" ").length;
	}

	public static boolean isPunctuation(char c) {
		return c == ',' || c == '.' || c == '!' || c == '?' || c == ':' || c == ';';
	}

	public static boolean isTermMatch(final String temp, final RareWordTerm rareWordHit,
			final List<FastLookupToken> allTokens, final int termStartIndex, final int termEndIndex) {

//		LOGGER.info("isTermMatch Started");
		String inputToken = "";
		for (int i = termStartIndex; i <= termEndIndex; i++) {
			try {
				inputToken += allTokens.get(i).getText() + " ";
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		inputToken = inputToken.trim();

		String rareWord = rareWordHit.getText();

		if (rareWord.equalsIgnoreCase("causa Cardio embolico")) {
			int check = 0;
		}

//		LOGGER.info("rareWord: " + rareWordHit.getText());

		String tempText = inputToken;
//		LOGGER.info("inputToken: " + inputToken);

		RemoveAccents rc = new RemoveAccents();
		tempText = rc.removeAccents(tempText);
//		LOGGER.info("tempText: " + tempText);

		String[] listtempText = tempText.split(" ");
		listtempText[0] = rareWordHit.getRareWord();
		if (rareWordHit.getRareWord().equals("tc")) {
			listtempText[0] = "tac";
			String[] tempeareWord = rareWordHit.getText().split(" ");
			tempeareWord[0] = "tac";
			rareWord = String.join(" ", tempeareWord);

		}
		tempText = String.join(" ", listtempText);

		Map<String, List<String>> suggestions = spellchecker_Sent
				.checkWordsReturnErrorSuggestions(Arrays.asList(tempText), accuracy_sentence);

//		if ((suggestions.entrySet().stream().count() >= 1) && !suggestions.entrySet().stream().findFirst().get().getValue().isEmpty()) {
//			LOGGER.info("Suggestions: " + suggestions
//					.entrySet().stream().findFirst().get().getValue().get(0));
//		}
//		else {
//			LOGGER.info("Suggestions: ZERO!");
//
//		}
		String first_one = "";
		if (suggestions.entrySet().stream().count() >= 1
				&& !suggestions.entrySet().stream().findFirst().get().getValue().isEmpty()) {
			first_one = suggestions.entrySet().stream().findFirst().get().getValue().get(0);
			for (Map.Entry<String, List<String>> listEntry : suggestions.entrySet()) {
				// iterating over a list
				for (String suggested_one : listEntry.getValue()) {
					if (suggested_one.split(" ").length == termEndIndex - termStartIndex + 1) {
						first_one = suggested_one;
						break;
					}
				}
			}
		}

		try {
			if (((suggestions.entrySet().stream().count() >= 1
					&& !suggestions.entrySet().stream().findFirst().get().getValue().isEmpty()
					&& (first_one.equalsIgnoreCase(rareWord)))) || tempText.equalsIgnoreCase(rareWord)) {
//				LOGGER.info("OK isTERMMatch: " + rareWord);
				return true;
			} else {
//				LOGGER.info("isTermMatch Ended");
				return false;
			}

		} catch (Exception e) {
			System.out.println("ERROR" + inputToken);
		}

		return false;

	}

	private static Exception str(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	static public AnalysisEngineDescription createAnnotatorDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(DefaultJCasTermAnnotator.class);
	}

	static public AnalysisEngineDescription createAnnotatorDescription(final String descriptorPath,
			final Boolean lemmaForm) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(DefaultJCasTermAnnotator.class,
				ConfigParameterConstants.PARAM_LOOKUP_XML, descriptorPath, ConfigParameterConstants.PARAM_LEMMA,
				lemmaForm);
	}

}
