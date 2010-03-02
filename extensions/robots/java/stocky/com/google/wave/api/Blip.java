/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.wave.api;


import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * A class that models a single blip instance.
 *
 * Blips are essentially the documents that make up a conversation, that contain
 * annotations, content and elements.
 */
public class Blip {

  /** The id of this blip. */
  private final String blipId;

  /** The id of the wave that owns this blip. */
  private final WaveId waveId;

  /** The id of the wavelet that owns this blip. */
  private final WaveletId waveletId;

  /** The id of the parent blip, {@code null} for a root blip. */
  private final String parentBlipId;

  /** The ids of the children of this blip. */
  private final List<String> childBlipIds;

  /** The participant ids of the contributors of this blip. */
  private final List<String> contributors;

  /** The participant id of the creator of this blip. */
  private final String creator;

  /** The last modified time of this blip. */
  private final long lastModifiedTime;

  /** The version of this blip. */
  private final long version;

  /** The list of annotations for the content. */
  private final Annotations annotations;

  /** The wavelet that owns this blip. */
  @NonJsonSerializable private final Wavelet wavelet;

  /** The operation queue to queue operation to the robot proxy. */
  @NonJsonSerializable private final OperationQueue operationQueue;

  /** The blip content. */
  private String content;

  /** The element contents of this blip. */
  private SortedMap<Integer, Element> elements;

  /**
   * Constructor.
   *
   * @param blipId the id of this blip.
   * @param initialContent the initial content of the blip.
   * @param parentBlipId the id of the parent.
   * @param wavelet the wavelet that owns this blip.
   */
  Blip(String blipId, String initialContent, String parentBlipId, Wavelet wavelet) {
    this(blipId, new ArrayList<String>(), initialContent, new ArrayList<String>(), null, -1, -1,
        parentBlipId, new ArrayList<Annotation>(), new TreeMap<Integer, Element>(), wavelet);

    // Make sure that initial content is valid, and starts with newline.
    if (this.content == null || this.content.isEmpty()) {
      this.content = "\n";
    } else if (!this.content.startsWith("\n")) {
      this.content = "\n" + this.content;
    }
  }

  /**
   * Constructor.
   *
   * @param blipId the id of this blip.
   * @param childBlipIds he ids of the children of this blip.
   * @param content the content of this blip.
   * @param contributors the participant ids of the contributors of this blip.
   * @param creator the participant id of the creator of this blip.
   * @param lastModifiedTime the last modified time of this blip.
   * @param version the version of this blip.
   * @param parentBlipId the id of the parent of this blip.
   * @param annotations the list of annotations for this blip's content.
   * @param elements the element contents of this blip.
   * @param wavelet the wavelet that owns this blip.
   */
  Blip(String blipId, List<String> childBlipIds, String content, List<String> contributors,
      String creator, long lastModifiedTime, long version, String parentBlipId,
      List<Annotation> annotations, Map<Integer, Element> elements, Wavelet wavelet) {
    this.blipId = blipId;
    this.waveId = wavelet.getWaveId();
    this.waveletId = wavelet.getWaveletId();
    this.content = content;
    this.childBlipIds = new ArrayList<String>(childBlipIds);
    this.contributors = new ArrayList<String>(contributors);
    this.creator = creator;
    this.lastModifiedTime = lastModifiedTime;
    this.version = version;
    this.parentBlipId = parentBlipId;

    this.annotations = new Annotations();
    for (Annotation annotation : annotations) {
      this.annotations.add(annotation.getName(), annotation.getValue(),
          annotation.getRange().getStart(), annotation.getRange().getEnd());
    }

    this.elements = new TreeMap<Integer, Element>(elements);
    this.wavelet = wavelet;
    this.operationQueue = wavelet.getOperationQueue();
  }

  /**
   * Shallow copy constructor.
   *
   * @param other the blip to copy.
   * @param operationQueue the operation queue for this new blip instance.
   */
  private Blip(Blip other, OperationQueue operationQueue) {
    this.blipId = other.blipId;
    this.waveId = other.waveId;
    this.waveletId = other.waveletId;
    this.childBlipIds = other.childBlipIds;
    this.content = other.content;
    this.contributors = other.contributors;
    this.creator = other.creator;
    this.lastModifiedTime = other.lastModifiedTime;
    this.version = other.version;
    this.parentBlipId = other.parentBlipId;
    this.annotations = other.annotations;
    this.elements = other.elements;
    this.wavelet = other.wavelet;
    this.operationQueue = operationQueue;
  }

  /**
   * Returns the id of this blip.
   *
   * @return the blip id.
   */
  public String getBlipId() {
    return blipId;
  }

  /**
   * Returns the id of the wave that owns this blip.
   *
   * @return the wave id.
   */
  public WaveId getWaveId() {
    return waveId;
  }

  /**
   * Returns the id of the wavelet that owns this blip.
   *
   * @return the wavelet id.
   */
  public WaveletId getWaveletId() {
    return waveletId;
  }

  /**
   * Returns the list of ids of this blip children.
   *
   * @return the children's ids.
   */
  public List<String> getChildBlipIds() {
    return childBlipIds;
  }

  /**
   * Returns the list of child blips.
   *
   * @return the children of this blip.
   */
  public List<Blip> getChildBlips() {
    List<Blip> result = new ArrayList<Blip>(childBlipIds.size());
    for (String childId : childBlipIds) {
      Blip childBlip = wavelet.getBlips().get(childId);
      if (childBlip != null) {
        result.add(childBlip);
      }
    }
    return result;
  }

  /**
   * Returns the participant ids of the contributors of this blip.
   *
   * @return the blip's contributors.
   */
  public List<String> getContributors() {
    return contributors;
  }

  /**
   * Returns the participant id of the creator of this blip.
   *
   * @return the blip's creator.
   */
  public String getCreator() {
    return creator;
  }

  /**
   * Returns the last modified time of this blip.
   *
   * @return the blip's last modified time.
   */
  public long getLastModifiedTime() {
    return lastModifiedTime;
  }

  /**
   * Returns the version of this blip.
   *
   * @return the blip's version.
   */
  public long getVersion() {
    return version;
  }

  /**
   * Returns the id of this blip's parent. {@code null} if this is a root blip.
   *
   * @return the blip's parent's id.
   */
  public String getParentBlipId() {
    return parentBlipId;
  }

  /**
   * Returns the parent blip.
   *
   * @return the parent of this blip.
   */
  public Blip getParentBlip() {
    if (parentBlipId == null) {
      return null;
    }
    return wavelet.getBlips().get(parentBlipId);
  }

  /**
   * Checks whether this is a root blip or not.
   *
   * @return {@code true} if this is a root blip, denoted by {@code null} parent
   *     id.
   */
  public boolean isRoot() {
    return parentBlipId == null;
  }

  /**
   * Returns the annotations for this blip's content.
   *
   * @return the blip's annotations.
   */
  public Annotations getAnnotations() {
    return annotations;
  }

  /**
   * Returns the elements content of this blip.
   *
   * @return the blip's elements.
   */
  public SortedMap<Integer, Element> getElements() {
    return elements;
  }

  /**
   * Returns the text content of this blip.
   *
   * @return blip's content.
   */
  public String getContent() {
    return content;
  }

  /**
   * Sets the content of this blip.
   *
   * @param content the blip's content.
   */
  void setContent(String content) {
    if (!content.startsWith("\n")) {
      content = "\n" + content;
    }
    this.content = content;
  }

  /**
   * Returns the length/size of the blip, denoted by the length of this blip's
   * text content.
   *
   * @return the size of the blip.
   */
  public int length() {
    return content.length();
  }

  /**
   * Returns the wavelet that owns this Blip.
   *
   * @return the wavelet.
   */
  public Wavelet getWavelet() {
    return wavelet;
  }

  /**
   * Returns the operation queue for sending outgoing operations to the robot
   * proxy.
   *
   * @return the operation queue.
   */
  protected OperationQueue getOperationQueue() {
    return operationQueue;
  }

  /**
   * Returns a reference to the entire content of the blip.
   *
   * @return an instance of {@link BlipContentRefs}.
   */
  public BlipContentRefs all() {
    return BlipContentRefs.all(this);
  }

  /**
   * Returns all references to this blip's content that match {@code target}.
   *
   * @param target the text to search for.
   * @return an instance of {@link BlipContentRefs}.
   */
  public BlipContentRefs all(String target) {
    return BlipContentRefs.all(this, target, -1);
  }

  /**
   * Returns all references to this blip's content that match {@code target}.
   * This blip references object will have at most {@code maxResult} hits.
   *
   * @param target the text to search for.
   * @param maxResult the maximum number of hits. Specify -1 for no limit.
   * @return an instance of {@link BlipContentRefs}.
   */
  public BlipContentRefs all(String target, int maxResult) {
    return BlipContentRefs.all(this, target, maxResult);
  }

  /**
   * Returns all references to this blip's content that match {@code target} and
   * {@code restrictions}.
   *
   * @param target the element type to search for.
   * @param restrictions the element properties that need to be matched.
   * @return an instance of {@link BlipContentRefs}.
   */
  public BlipContentRefs all(ElementType target, Restriction... restrictions) {
    return BlipContentRefs.all(this, target, -1, restrictions);
  }

  /**
   * Returns all references to this blip's content that match {@code target} and
   * {@code restrictions}. This blip references object will have at most
   * {@code maxResult} hits.
   *
   * @param target the element type to search for.
   * @param maxResult the maximum number of hits. Specify -1 for no limit.
   * @param restrictions the element properties that need to be matched.
   * @return an instance of {@link BlipContentRefs}.
   */
  public BlipContentRefs all(ElementType target, int maxResult, Restriction... restrictions) {
    return BlipContentRefs.all(this, target, maxResult, restrictions);
  }

  /**
   * Returns the first reference to this blip's content that matches
   * {@code target}.
   *
   * @param target the text to search for.
   * @return an instance of {@link BlipContentRefs}.
   */
  public BlipContentRefs first(String target) {
    return all(target, 1);
  }

  /**
   * Returns the first reference to this blip's content that matches
   * {@code target} and {@code restrictions}.
   *
   * @param target the type of element to search for.
   * @param restrictions the list of restrictions to filter the search.
   * @return an instance of {@link BlipContentRefs}.
   */
  public BlipContentRefs first(ElementType target, Restriction... restrictions) {
    return all(target, 1, restrictions);
  }

  /**
   * Returns the reference to this blip's content at the specified index.
   *
   * @param index the index to reference.
   * @return an instance of {@link BlipContentRefs}.
   */
  public BlipContentRefs at(int index) {
    return BlipContentRefs.range(this, index, index + 1);
  }

  /**
   * Returns the reference to this blip's content at the specified range.
   *
   * @param start the start index of the range to reference.
   * @param end the end index of the range to reference.
   * @return an instance of {@link BlipContentRefs}.
   */
  public BlipContentRefs range(int start, int end) {
    return BlipContentRefs.range(this, start, end);
  }

  /**
   * Appends the given argument (element, text, or markup) to the blip.
   *
   * @param argument the element, text, or markup to be appended.
   * @return an instance of {@link BlipContentRefs}.
   */
  public BlipContentRefs append(BlipContent argument) {
    return BlipContentRefs.all(this).insertAfter(argument);
  }

  /**
   * Appends the given string to the blip.
   *
   * @param argument the string to be appended.
   * @return an instance of {@link BlipContentRefs}.
   */
  public BlipContentRefs append(String argument) {
    return BlipContentRefs.all(this).insertAfter(argument);
  }

  /**
   * Creates a reply to this blip.
   *
   * @return an instance of {@link Blip} that represents a reply to the blip.
   */
  public Blip reply() {
    return operationQueue.createChildOfBlip(this);
  }

  /**
   * Inserts an inline blip at the given position.
   *
   * @param position the index to insert the inline blip at.
   * @return an instance of {@link Blip} that represents the new inline blip.
   */
  public Blip insertInlineBlip(int position) {
    return operationQueue.insertInlineBlipToDocument(this, position);
  }

  /**
   * Returns a view of this blip that will proxy for the specified id.
   *
   * A shallow copy of the current blip is returned with the {@code proxyingFor}
   * field set. Any modifications made to this copy will be done using the
   * {@code proxyForId}, i.e. the {@code robot+<proxyForId>@appspot.com} address
   * will be used.
   *
   * @param proxyForId the id to proxy.
   * @return a shallow copy of this blip with the proxying information set.
   */
  public Blip proxyFor(String proxyForId) {
    OperationQueue proxiedOperationQueue = operationQueue.proxyFor(proxyForId);
    return new Blip(this, proxiedOperationQueue);
  }

  /**
   * Moves all elements and annotations after the given position by
   * {@code shiftAmount}.
   *
   * @param position the anchor position.
   * @param shiftAmount the amount to shift the annotations range and elements
   *    position.
   */
  protected void shift(int position, int shiftAmount) {
    SortedMap<Integer, Element> newElements =
        new TreeMap<Integer, Element>(elements.headMap(position));
    for (Entry<Integer, Element> element : elements.tailMap(position).entrySet()) {
      newElements.put(element.getKey() + shiftAmount, element.getValue());
    }
    this.elements = newElements;
    this.annotations.shift(position, shiftAmount);
  }

  /**
   * Deletes all annotations that span from {@code start} to {@code end}.
   *
   * @param start the start position.
   * @param end the end position.
   */
  protected void deleteAnnotations(int start, int end) {
    for (String name : annotations.namesSet()) {
      annotations.delete(name, start, end);
    }
  }

  /**
   * Deletes the given blip id from the list of child blip ids.
   *
   * @param childBlipId the blip id to delete.
   */
  protected void deleteChildBlipId(String childBlipId) {
    this.childBlipIds.remove(childBlipId);
  }

  /**
   * Serializes this {@link Blip} into a {@link BlipData}.
   *
   * @return an instance of {@link BlipData} that represents this blip.
   */
  public BlipData serialize() {
    BlipData blipData = new BlipData();

    // Add primitive properties.
    blipData.setBlipId(blipId);
    blipData.setWaveId(waveId.serialise());
    blipData.setWaveletId(waveletId.serialise());
    blipData.setParentBlipId(parentBlipId);
    blipData.setCreator(creator);
    blipData.setLastModifiedTime(lastModifiedTime);
    blipData.setVersion(version);
    blipData.setContent(content);

    // Add list and map properties.
    blipData.setChildBlipIds(childBlipIds);
    blipData.setContributors(contributors);
    blipData.setElements(elements);

    // Add annotations.
    List<Annotation> annotations = new ArrayList<Annotation>();
    for (Annotation annotation : this.annotations) {
      annotations.add(annotation);
    }
    blipData.setAnnotations(annotations);

    return blipData;
  }

  /**
   * Deserializes the given {@link BlipData} object into an instance of
   * {@link Blip}.
   *
   * @param operationQueue the operation queue.
   * @param wavelet the wavelet that owns this blip.
   * @param blipData the blip data to be deserialized.
   * @return an instance of {@link Wavelet}.
   */
  public static Blip deserialize(OperationQueue operationQueue, Wavelet wavelet,
      BlipData blipData) {
    // Extract primitive properties.
    String blipId = blipData.getBlipId();
    String parentBlipId = blipData.getParentBlipId();
    String creator = blipData.getCreator();
    long lastModifiedTime = blipData.getLastModifiedTime();
    long version = blipData.getVersion();
    String content = blipData.getContent();

    List<String> childBlipIds = blipData.getChildBlipIds();
    List<String> contributors = blipData.getContributors();
    Map<Integer, Element> elements = blipData.getElements();

    List<Annotation> annotations = blipData.getAnnotations();
    return new Blip(blipId, childBlipIds, content, contributors, creator, lastModifiedTime,
        version, parentBlipId, annotations, elements, wavelet);
  }
}
