/**
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.inputmethod.latin.dicttool;

import com.android.inputmethod.latin.makedict.FormatSpec;
import com.android.inputmethod.latin.makedict.FusionDictionary;
import com.android.inputmethod.latin.makedict.FusionDictionary.CharGroup;
import com.android.inputmethod.latin.makedict.FusionDictionary.WeightedString;
import com.android.inputmethod.latin.makedict.Word;

import java.util.ArrayList;

public class Info extends Dicttool.Command {
    public static final String COMMAND = "info";

    public Info() {
    }

    @Override
    public String getHelp() {
        return COMMAND + "<filename>: prints various information about a dictionary file";
    }

    private static void showInfo(final FusionDictionary dict) {
        System.out.println("Header attributes :");
        System.out.print(dict.mOptions.toString(2));
        int wordCount = 0;
        int bigramCount = 0;
        int shortcutCount = 0;
        int whitelistCount = 0;
        for (final Word w : dict) {
            ++wordCount;
            if (null != w.mBigrams) {
                bigramCount += w.mBigrams.size();
            }
            if (null != w.mShortcutTargets) {
                shortcutCount += w.mShortcutTargets.size();
                for (WeightedString shortcutTarget : w.mShortcutTargets) {
                    if (FormatSpec.SHORTCUT_WHITELIST_FREQUENCY == shortcutTarget.mFrequency) {
                        ++whitelistCount;
                    }
                }
            }
        }
        System.out.println("Words in the dictionary : " + wordCount);
        System.out.println("Bigram count : " + bigramCount);
        System.out.println("Shortcuts : " + shortcutCount + " (out of which " + whitelistCount
                + " whitelist entries)");
    }

    private static void showWordInfo(final FusionDictionary dict, final String word) {
        final CharGroup group = dict.findWordInTree(dict.mRoot, word);
        if (null == group) {
            System.out.println(word + " is not in the dictionary");
            return;
        }
        System.out.println("Word: " + word);
        System.out.println("  Freq: " + group.getFrequency());
        if (group.getIsNotAWord()) {
            System.out.println("  Is not a word");
        }
        if (group.getIsBlacklistEntry()) {
            System.out.println("  Is a blacklist entry");
        }
        final ArrayList<WeightedString> shortcutTargets = group.getShortcutTargets();
        if (null == shortcutTargets || shortcutTargets.isEmpty()) {
            System.out.println("  No shortcuts");
        } else {
            for (final WeightedString shortcutTarget : shortcutTargets) {
                System.out.println("  Shortcut target: " + shortcutTarget.mWord + " ("
                        + (FormatSpec.SHORTCUT_WHITELIST_FREQUENCY == shortcutTarget.mFrequency
                                ? "whitelist" : shortcutTarget.mFrequency) + ")");
            }
        }
        final ArrayList<WeightedString> bigrams = group.getBigrams();
        if (null == bigrams || bigrams.isEmpty()) {
            System.out.println("  No bigrams");
        } else {
            for (final WeightedString bigram : bigrams) {
                System.out.println("  Bigram: " + bigram.mWord + " (" + bigram.mFrequency + ")");
            }
        }
    }

    @Override
    public void run() {
        if (mArgs.length < 1) {
            throw new RuntimeException("Not enough arguments for command " + COMMAND);
        }
        final String filename = mArgs[0];
        final boolean hasWordArguments = (1 == mArgs.length);
        final FusionDictionary dict = BinaryDictOffdeviceUtils.getDictionary(filename,
                hasWordArguments /* report */);
        if (hasWordArguments) {
            showInfo(dict);
        } else {
            for (int i = 1; i < mArgs.length; ++i) {
                showWordInfo(dict, mArgs[i]);
            }
        }
    }
}
