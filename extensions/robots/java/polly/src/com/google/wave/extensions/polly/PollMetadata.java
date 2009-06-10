// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.polly;

import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.Wavelet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the persistent state of the poll stored in wave. This class
 * provides simple serialization/deserialization into a string. Data documents
 * can be arbitrary xml or string data.
 * 
 * @author scovitz@google.com (Seth Covitz)
 */
public class PollMetadata {

  // Delimeter and separator constants.
  private static final String LINE_SEP_REGEX = "\\n";
  private static final String FIELD_SEP_REGEX = "\\:";

  private static final String METADATA_DOC = "poll-metadata";
  private static final String METADATA_FIELD_SEP = "|";
  private static final String METADATA_FIELD_SEP_REGEX = "\\|";
  private static final String METADATA_ID_SEP = "$";
  private static final String METADATA_ID_SEP_REGEX = "\\$";

  private static final String NEWIDS_DOC = "_new_ids_";
  private static final String NEWIDS_POLL_BLIP_PREFIX = "poll-blip-";
  
  private static final String VOTES_DOC = "poll-votes";

  /**
   * The previously stored title for an AdminWavelet or the distributed poll
   * title for a PollWavelet.
   */
  private String title = "";
  
  /**
   * The previously stored question for an AdminWavelet or the distributed poll
   * question for a PollWavelet.
   */
  private String question = "";

  /**
   * The previously stored list of choices for an AdminWavelet or the
   * distributed list of choices for a PollWavelet.
   */
  private List<String> choices = new ArrayList<String>();

  /**
   * The list of poll recipients for the AdminWavelet or the current pollee
   * for the PollWavelet.
   */
  private String recipients = "";
  
  /**
   * A map of recipients to PollWavelets.
   */
  private Map<String, String> pollWaveletBlipIds = new HashMap<String, String>();
  
  /**
   * A map of recipients to their votes. 
   */
  private Map<String, String> pollVotes = new HashMap<String, String>();
  
  /**
   * The AdminWavelet Id for PollWavelets to communicate their vote.
   */
  private String resultsWaveletId = "";

  /**
   * The serialized form of the data document to compare against to determine
   * changes. 
   */
  private String dataDocument = "";
  
  /**
   * The current robot context.
   */
  private RobotMessageBundle context;
  
  /**
   * Whether any of the poll metadata has changed (and should be saved).
   */
  private boolean hasChanged = true;
  
  /**
   * Whether new votes have been received since the last time the metadata was
   * read.
   */
  private boolean hasNewVotes = false;

  /**
   * Constructs the previous state of the Admin or Poll wavelet from metadata
   * stored in a data document. 
   * 
   * @param context the context the robot is currently called with.
   */
  public PollMetadata(RobotMessageBundle context) {
    this.context = context;
    loadMetadata();
    processNewIds();
    processNewVotes();
  }

  /**
   * Deserializes the previous state of the poll metadata from the data stored
   * in the data document.
   */
  private void loadMetadata() {
    this.dataDocument = context.getWavelet().getDataDocument(METADATA_DOC);
    if (dataDocument != null) {
      try {
        String[] items = dataDocument.split(METADATA_FIELD_SEP_REGEX);

        this.title = items[0];
        this.question = items[1];
        
        String choicesString = items[2];
        String[] choices = choicesString.split(METADATA_ID_SEP_REGEX);
        this.choices.clear();
        for (String choice : choices) {
          this.choices.add(choice);
        }

        this.recipients = items[3];
        this.resultsWaveletId = items[4];

        String pollWaveletBlipString = items[5];
        String[] pollBlips = pollWaveletBlipString.split(METADATA_ID_SEP_REGEX);
        for (int i = 0; i < pollBlips.length; i += 3) {
          pollWaveletBlipIds.put(pollBlips[i], pollBlips[i + 1]);
          pollVotes.put(pollBlips[i], pollBlips[i + 2]);
        }
      } catch(IndexOutOfBoundsException iobx) {
        // Receiving an exception means that the data document has not yet
        // been fully populated.
      }
      hasChanged = false;
    }
  }
  
  /**
   * Saves the poll metadata as a data document in the robot context's wavelet.
   */
  public void saveMetadata() {
    saveMetadata(context.getWavelet());
  }
  
  /**
   * Serializes the state of the poll metadata to the given wavelet. If the
   * new data document happens to be unchanged (ie. a person voted with the
   * same vote twice), the data document is not saved.
   * 
   * @param wavelet the wavelet in which to persist the data document.
   */
  public void saveMetadata(Wavelet wavelet) {
    StringBuilder pollWaveletBlipString = new StringBuilder();
    for (Map.Entry<String, String> pollBlip : pollWaveletBlipIds.entrySet()) {
      pollWaveletBlipString.append(METADATA_ID_SEP);
      pollWaveletBlipString.append(pollBlip.getKey());
      pollWaveletBlipString.append(METADATA_ID_SEP);
      pollWaveletBlipString.append(pollBlip.getValue());
      pollWaveletBlipString.append(METADATA_ID_SEP);
      pollWaveletBlipString.append(pollVotes.get(pollBlip.getKey()));
    }
    if (pollWaveletBlipString.length() > 0) {
      pollWaveletBlipString.deleteCharAt(0);
    }
    
    StringBuilder choicesString = new StringBuilder();
    for (String choice : this.choices) {
      choicesString.append(METADATA_ID_SEP);
      choicesString.append(choice);
    }
    if (choicesString.length() > 0) {
      choicesString.deleteCharAt(0);
    }
    
    StringBuilder newDataDocument = new StringBuilder();
    
    newDataDocument.append(title);
    newDataDocument.append(METADATA_FIELD_SEP);
    newDataDocument.append(question);
    newDataDocument.append(METADATA_FIELD_SEP);
    newDataDocument.append(choicesString);
    newDataDocument.append(METADATA_FIELD_SEP);
    newDataDocument.append(recipients);
    newDataDocument.append(METADATA_FIELD_SEP);
    newDataDocument.append(resultsWaveletId);
    newDataDocument.append(METADATA_FIELD_SEP);
    newDataDocument.append(pollWaveletBlipString);
    
    if (!newDataDocument.toString().equals(dataDocument)) {
      wavelet.setDataDocument(METADATA_DOC, newDataDocument.toString());
    }
  }

  /**
   * Checks for the presence of the _new_ids_ data document and if present,
   * processes and stores the newly created poll wavelet ids.
   */
  private void processNewIds() {
    String newIds = context.getWavelet().getDataDocument(NEWIDS_DOC);
    if (newIds != null && !newIds.isEmpty()) {
      try {
        for (String line : newIds.split(LINE_SEP_REGEX)) {
          if (line.startsWith(NEWIDS_POLL_BLIP_PREFIX)) {
            String[] pollBlip = line.split(FIELD_SEP_REGEX);
            String participant = pollBlip[0].substring(NEWIDS_POLL_BLIP_PREFIX.length());
            String participantPollBlipId = pollBlip[1];
            pollWaveletBlipIds.put(participant, participantPollBlipId);
            hasChanged = true;
          }
        }
      } catch(IndexOutOfBoundsException iobx) {
        // Receiving an exception means that the data document has not yet
        // been populated.
      }
      context.getWavelet().setDataDocument(NEWIDS_DOC, "");
    }
  }

  /**
   * Checks for the presence of the poll-votes data document and if present,
   * process and stores any newly received votes. Only a single vote is stored
   * per pollee so the most recent vote overrides any previous votes.
   */
  private void processNewVotes() {
    String newVotes = context.getWavelet().getDataDocument(VOTES_DOC);
    if (newVotes != null && !newVotes.isEmpty()) {
      try {
        for (String line : newVotes.split(LINE_SEP_REGEX)) {
          String[] vote = line.split(FIELD_SEP_REGEX);
          String participant = vote[0];
          String choice = vote[1];
          pollVotes.put(participant, choice);
          hasChanged = true;
          hasNewVotes = true;
        }
      } catch(IndexOutOfBoundsException iobx) {
        // Receiving an exception means that the data document has not yet
        // been populated.
      }
      context.getWavelet().setDataDocument(VOTES_DOC, "");
    }
  }

  /**
   * Returns whether the metadata has changed. This can be caused by the
   * addition of new polls or votes, or the updating of metadata by changes in
   * the AdminWavelet.
   * 
   * @return true if the metadata has changed, false otherwise.
   */
  public boolean hasChanged() {
    return hasChanged;
  }

  /**
   * Returns the poll title.
   * 
   * @return the poll title.
   */
  public String getTitle() {
    return title;
  }
  
  /**
   * Returns the poll question.
   * 
   * @return the poll question.
   */
  public String getQuestion() {
    return question;
  }

  /**
   * Returns the list of recipients.
   * 
   * @return the list of recipients.
   */
  public String getRecipients() {
    return recipients;
  }

  /**
   * Returns the list of choices.
   * 
   * @return the list of choices.
   */
  public List<String> getChoices() {
    return choices;
  }

  /**
   * Accumulates the current vote tally for each choice and return it as a
   * list.
   * 
   * @return the list of votes for each choice.
   */
  public List<Integer> getChoiceCounts() {
    // Initialize the counts to zero.
    List<Integer> counts = new ArrayList<Integer>();
    for (String choice : choices) {
      counts.add(0);
    }

    // Accumulate non-null votes.
    for (String vote : pollVotes.values()) {
      if (!"null".equals(vote)) {
        int voteIndex = Integer.parseInt(vote);
        counts.set(voteIndex, counts.get(voteIndex) + 1);
      }
    }
    
    return counts;
  }

  /**
   * Converts the comma separated list of recipients into a list.
   * 
   * @return a list of recipients.
   */
  public List<String> getRecipientsAsList() {
    List<String> recipientList = new ArrayList<String>();
    if (!recipients.isEmpty()) {
      for (String recipient : recipients.split(",")) {
        recipientList.add(recipient.trim());
      }
    }
    return recipientList;
  }

  /**
   * Returns the list of recipients that have voted.
   * 
   * @return the list of recipients that have voted.
   */
  public List<String> getVoters() {
    List<String> voters = new ArrayList<String>();
    for (Map.Entry<String, String> vote : pollVotes.entrySet()) {
      if (!"null".equals(vote.getValue())) {
        voters.add(vote.getKey());
      }
    }
    return voters;
  }

  /**
   * Sets the poll title.
   * 
   * @param title the new poll title value.
   */
  public void setTitle(String title) {
    this.title = title;
    hasChanged = true;
  }
  
  /**
   * Sets the poll question.
   * 
   * @param question the new poll question value.
   */
  public void setQuestion(String question) {
    this.question = question;
    hasChanged = true;
  }
  
  /**
   * Sets the list of choices.
   * 
   * @param choices the new list of choices.
   */
  public void setChoices(List<String> choices) {
    this.choices = choices;
    hasChanged = true;
  }

  /**
   * Sets the list of recipients.
   * 
   * @param recipients the new list of recipients.
   */
  public void setRecipients(String recipients) {
    this.recipients = recipients;
    hasChanged = true;
  }

  /**
   * Sets the results wavelet id. This is used mainly by PollWavelets to
   * communicate back their votes. The results wavelet is synonymous with
   * the AdminWavelet.
   * 
   * @param resultsWaveletId
   */
  public void setResultsWaveletId(String resultsWaveletId) {
    this.resultsWaveletId = resultsWaveletId;
    hasChanged = true;
  }

  /**
   * Returns the results wavelet id. 
   * 
   * @return the results wavelet id.
   */
  public String getResultsWaveletId() {
    return resultsWaveletId;
  }

  /**
   * Writes a pollee's vote to the votes data document of the results wavelet.
   * 
   * @param vote the choice that the user selected.
   */
  public void writeVoteToResults(String vote) {
    String[] waveIdParts = resultsWaveletId.split(" ");
    String waveId = waveIdParts[0];
    String waveletId = waveIdParts[1];
    Wavelet resultsWavelet = context.getWavelet(waveId, waveletId);
    
    // Append is used rather than set to support concurrent access by pollees
    // voting at the same time.
    resultsWavelet.appendDataDocument(VOTES_DOC,
        recipients + ":" + vote + "\n");
  }

  /**
   * Returns whether new votes have been detected in the metadata. This is
   * useful for updating the results pane display.
   * 
   * @return true if there are new votes, false otherwise.
   */
  public boolean hasNewVotes() {
    return hasNewVotes;
  }

  /**
   * Constructs a string to identify the new poll wavelet id for a recipient.
   * Writebacks are necessary as the wavelet isn't created until the operation
   * is received by the RobotProxy server. In order to access a wavelet
   * without an event having been generated, one will need the id. Hence a
   * writeback mechanism is used as a notification of the newly created id once
   * it is available.
   * 
   * @param recipient
   * @return the key for the writeback id.
   */
  public String getPollWriteback(String recipient) {
    return NEWIDS_POLL_BLIP_PREFIX + recipient;
  }

  /**
   * Determines whether a given recipient has voted or not.
   * 
   * @param recipient the recipient to check.
   * @return true if the recipient has voted, false otherwise.
   */
  public boolean hasVoter(String recipient) {
    return pollVotes.containsKey(recipient);
  }

  /**
   * Adds a recipient to the list of voters. A 'null' vote is added by default.
   * 
   * @param recipient the recipient to be added.
   */
  public void addVoter(String recipient) {
    if (!hasVoter(recipient)) {
      pollVotes.put(recipient, "null");
      hasChanged = true;
    }
  }
}
