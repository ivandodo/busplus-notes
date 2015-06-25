/*
 * Copyright (C) 2010 The Android Open Source Project
 * Copyright (C) 2011 Adam Nybäck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ivandodo.busplusnotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ivandodo.busplusnotes.adapters.BusplusCardViewAdapter;
import com.ivandodo.busplusnotes.entity.BusPlusCard;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * An {@link Activity} which handles a broadcast of a new tag that the device just discovered.
 */
public class TagViewer extends Activity {

    private static final DateFormat TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
    private static final String MY_CARDS = "myCards";
    private ArrayList<BusPlusCard> BUS_PLUS_CARDS;
    private LinearLayout mTagContent;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;
    private String MY_PREFS_NAME = "BusPlusNotesPrefs";

    private AlertDialog mDialog;
    private RecyclerView mCardsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.tag_viewer);
            resolveIntent(getIntent());

            mDialog = new AlertDialog.Builder(this).setNeutralButton("Ok", null).create();

            mAdapter = NfcAdapter.getDefaultAdapter(this);
            if (mAdapter == null) {
                showMessage(R.string.error, R.string.no_nfc);
                finish();
                return;
            }

            Gson gson = new Gson();

            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            String restoredText = prefs.getString(MY_CARDS, null);
            if (restoredText != null) {
                BUS_PLUS_CARDS = gson.fromJson(restoredText, new TypeToken<ArrayList<BusPlusCard>>(){}.getType());
            } else {
                BUS_PLUS_CARDS = new ArrayList<BusPlusCard>();
            }

            mCardsList = (RecyclerView) findViewById(R.id.cardsRecyclerView);
            BusplusCardViewAdapter adapter = new BusplusCardViewAdapter(this);
            adapter.setOnItemClickListener(new BusplusCardViewAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BusPlusCard entity) {
                }
            });
            adapter.setData(BUS_PLUS_CARDS);
            mCardsList.setAdapter(adapter);
            mCardsList.setLayoutManager(new LinearLayoutManager(this));


            mPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            mNdefPushMessage = new NdefMessage(new NdefRecord[]{newTextRecord(
                    "Message from NFC Reader :-)", Locale.ENGLISH, true)});
        }catch(Throwable t){
            Log.e("Puko","Puko", t);
        }
    }

    private void addCard(BusPlusCard card){
        BUS_PLUS_CARDS.add(0, card);

        Gson gson = new Gson();
        String str = gson.toJson(BUS_PLUS_CARDS);
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(MY_CARDS, str);
        editor.commit();
    }

    private void showMessage(int title, int message) {
        mDialog.setTitle(title);
        mDialog.setMessage(getText(message));
        mDialog.show();
    }

    private NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) {
                showWirelessSettingsDialog();
            }
            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
            mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
            mAdapter.disableForegroundNdefPush(this);
        }
    }

    private void showWirelessSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.nfc_disabled);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.create().show();
        return;
    }

    private void resolveIntent(Intent intent) {
        try{
        String action = intent.getAction();
        String id = null;
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                //Nije kartica, vec nfc tag, javiti gresku
                Toast t = Toast.makeText(this, "Nije BusPlusKartica", Toast.LENGTH_SHORT);
                t.show();
            } else {
                // Unknown tag type
                Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                id = dumpTagId(tag);
            }
            //Provera da li kartica postoji. Ako ne, ide na kreiranje novu, ako postoji, daje opcije
            if (id!= null) {
                BusPlusCard card = null;
                for (BusPlusCard c : BUS_PLUS_CARDS) {
                    if (c.getSerial().equals(id)){
                        card = c;
                    }
                }

                if (card != null) {
                    Toast t = Toast.makeText(this, "Ocitana kartica: " + card.getName() + " (" + card.getSerial() + ")", Toast.LENGTH_SHORT);
                    t.show();
                }
                else{
                    BusPlusCard c = new BusPlusCard("MyCard", id, 1000.00, BusPlusCard.Type.PREPAID);
                    addCard(c);
                    ((BusplusCardViewAdapter)mCardsList.getAdapter()).setData(BUS_PLUS_CARDS);
                    mCardsList.getAdapter().notifyDataSetChanged();
                }
            }
        }
        }catch(Throwable t){
            Log.e("Puko","Puko", t);
        }
    }

    private String dumpTagId(Parcelable p) {
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append(getDec(id));
        return sb.toString();
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }
}