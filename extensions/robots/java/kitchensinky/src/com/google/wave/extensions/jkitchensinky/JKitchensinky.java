/* Copyright (c) 2010 Google Inc.
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

package com.google.wave.extensions.jkitchensinky;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.wave.api.AbstractRobot;
import com.google.wave.api.Blip;
import com.google.wave.api.BlipData;
import com.google.wave.api.Context;
import com.google.wave.api.ElementType;
import com.google.wave.api.FormElement;
import com.google.wave.api.Gadget;
import com.google.wave.api.Image;
import com.google.wave.api.Installer;
import com.google.wave.api.Line;
import com.google.wave.api.Markup;
import com.google.wave.api.OperationQueue;
import com.google.wave.api.ParticipantProfile;
import com.google.wave.api.Wavelet;
import com.google.wave.api.event.BlipSubmittedEvent;
import com.google.wave.api.event.OperationErrorEvent;
import com.google.wave.api.event.WaveletCreatedEvent;
import com.google.wave.api.event.WaveletSelfAddedEvent;
import com.google.wave.api.impl.GsonFactory;
import com.google.wave.api.impl.WaveletData;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A robot that exercises basic functionality of Google Wave Robot API using the
 * Java client library. One of the exercise that this robot does is inserting a
 * gadget that does embedding.
 */
public class JKitchensinky extends AbstractRobot {

  /** Logger. */
  private static final Logger LOG = Logger.getLogger(JKitchensinky.class.getName());

  /** Serializer to serialize wavelet. */
  private static final Gson SERIALIZER = new GsonFactory().create();

  @Override
  public void onBlipSubmitted(BlipSubmittedEvent e) {
    // Invoked when any blip we are interested in is submitted.
    LOG.info("onBlipSubmitted");
    Blip blip = e.getBlip();

    String gadgetUrl = "http://jkitchensinky.appspot.com/public/embed.xml";
    Gadget gadget = Gadget.class.cast(blip.first(ElementType.GADGET,
        Gadget.restrictByUrl(gadgetUrl)).value());
    if (gadget != null &&
        gadget.getProperty("loaded", "no").equals("yes") &&
        gadget.getProperty("seen", "no").equals("no")) {
      // Elements should always be updated through a BlipContentRefs to
      // correspond the matching operations for the wire.
      blip.first(ElementType.GADGET, Gadget.restrictByUrl(gadgetUrl)).updateElement(
          ImmutableMap.of("seen", "yes"));
      blip.append("\nSeems all to have worked out.");
      blip.first(ElementType.IMAGE).updateElement(
          ImmutableMap.of("url", "http://www.google.com/logos/poppy09.gif"));
    }

    // Update installer either way.
    String extensionInstallerUrl = "http://google-wave-resources.googlecode.com/svn/trunk/" +
        "samples/extensions/gadgets/mappy/installer.xml";
    blip.first(ElementType.INSTALLER).updateElement(
        ImmutableMap.of("manifest", extensionInstallerUrl));
  }

  @Capability(contexts = {Context.SELF})
  @Override
  public void onWaveletSelfAdded(WaveletSelfAddedEvent e) {
    // Invoked when any participants have been added/removed from the wavelet.
    LOG.info("onWaveletSelfAdded");
    Blip blip = e.getBlip();
    Wavelet wavelet = e.getWavelet();

    // Test setting wavelet title.
    wavelet.setTitle("A wavelet title");

    // Test inserting image.
    blip.append(new Image("http://www.google.com/logos/clickortreat1.gif", 320, 118,
        "Click or treat"));

    // Test inserting list.
    Line line = new Line();
    line.setLineType("li");
    line.setIndent("2");
    blip.append(line);
    blip.append("bulleted!");

    // Test inserting extension installer.
    String installerUrl = "http://wave-skynet.appspot.com/public/extensions/areyouin/manifest.xml";
    blip.append(new Installer(installerUrl));

    // Test adding a proxied reply. The reply will be posted by
    // jkitchensinky+proxy@appspot.com. Note that as a side effect this will
    // also add this participant to the wave.
    wavelet.proxyFor("proxy").reply("\n").append("Douwe says Java rocks!");

    // Test inserting inline blip.
    Blip inlineBlip = blip.insertInlineBlip(5);
    inlineBlip.append("Hello again!");

    // Test creating a new wave. The new wave will have its own operation queue.
    // {@link AbstractRobot#newWave(String, Set, String, String)} takes a
    // {@code message} parameter which can be set to an arbitrary string. By
    // setting it to the serialized version of the current wave, we can
    // reconstruct the current wave when the other wave is constructed and
    // update the current wave.
    JsonElement waveletJson = SERIALIZER.toJsonTree(wavelet.serialize());
    JsonElement blipJson = SERIALIZER.toJsonTree(blip.serialize());
    JsonObject json = new JsonObject();
    json.add("wavelet", waveletJson);
    json.add("blip", blipJson);

    Wavelet newWave = this.newWave(wavelet.getDomain(), wavelet.getParticipants(),
        SERIALIZER.toJson(json), null);
    newWave.getRootBlip().append("A new day and a new wave");
    newWave.getRootBlip().append(Markup.of("<p>Some stuff!</p><p>Not the <b>beautiful</b></p>"));

    // Since the new wave has its own operation queue, we need to submit it
    // explicitly through the active gateway, or, as in this case, submit it
    // together with wavelet, which will handle the submit automatically.
    newWave.submitWith(wavelet);

    // Test inserting a form element.
    blip.append(new FormElement(ElementType.CHECK, "My Label", "true", "false"));

    // Test inserting replies with image.
    String imgUrl = "http://upload.wikimedia.org/wikipedia/en/thumb/c/cc/Googlewave.svg/" +
        "200px-Googlewave.svg.png";
    Blip replyBlip = blip.reply();
    replyBlip.append("Hello");
    replyBlip.append(new Image(imgUrl, 50, 50, "caption"));
    replyBlip.append("\nHello");
    replyBlip.append(new Image(imgUrl, 50, 50, "caption"));
    replyBlip.append("\nHello");
    replyBlip.append(new Image(imgUrl, 50, 50, "caption"));
  }

  @Override
  public void onWaveletCreated(WaveletCreatedEvent e) {
    // Invoked when the robot creates a new wave.
    LOG.info("onWaveletCreated");

    // Reconstruct the original wavelet. This is a "blind" wavelet since any
    // operations applied to this wavelet are done without us really knowing
    // what the state of the wavelet is (it might have changed on the server).
    // This means we have to be careful.
    JsonObject json = new JsonParser().parse(e.getMessage()).getAsJsonObject();
    WaveletData waveletData = SERIALIZER.fromJson(json.get("wavelet"), WaveletData.class);
    BlipData blipData = SERIALIZER.fromJson(json.get("blip"), BlipData.class);
    OperationQueue operationQueue = new OperationQueue();

    Map<String, Blip> blips = new HashMap<String, Blip>();
    Wavelet originalWavelet = Wavelet.deserialize(operationQueue, blips, waveletData);

    Blip rootBlip = Blip.deserialize(operationQueue, originalWavelet, blipData);
    blips.put(rootBlip.getBlipId(), rootBlip);

    // Add a gadget that embeds the newly created wave to the original wavelet.
    Gadget gadget = new Gadget("http://jkitchensinky.appspot.com/public/embed.xml");
    gadget.setProperty("waveid", e.getWavelet().getWaveId().serialise());
    originalWavelet.getRootBlip().append(gadget);

    // Insert some non-standard characters.
    String unicodeString = "\u0430\u0431\u0432";
    originalWavelet.getRootBlip().append("\nInserted a gadget: " + unicodeString);

    // Again, we have to explicitly submit the operations to the other wavelet.
    originalWavelet.submitWith(e.getWavelet());
  }

  @Override
  public void onOperationError(OperationErrorEvent e) {
    LOG.warning("Previous operation failed: id=" + e.getOperationId() + ", message=" +
        e.getMessage());
  }

  @Override
  protected String getRobotName() {
    return "JKitchensinky";
  }

  @Override
  protected String getRobotAvatarUrl() {
    return "http://jkitchensinky.appspot.com/public/avatar.png";
  }

  @Override
  protected String getRobotProfilePageUrl() {
    return "http://code.google.com/apis/wave/extensions/robots/java-tutorial.html";
  }

  @Override
  protected ParticipantProfile getCustomProfile(String name) {
    return new ParticipantProfile("Proxied JKitchensinky",
        "http://jkitchensinky.appspot.com/public/proxied-avatar.png",
        getRobotProfilePageUrl());
  }
}
