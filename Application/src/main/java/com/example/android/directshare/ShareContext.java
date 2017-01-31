/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.example.android.directshare;

import android.content.Context;

/**
 * Provides the list of dummy contacts. This sample implements this as constants, but real-life apps
 * should use a database and such.
 */
public class ShareContext {

    /**
     * The list of dummy contacts.
     */
    public static final ShareContext[] SHARE_CONTEXTS = {
            new ShareContext("Ethereum", "ethereum", "ethereum:0x1234"),
            new ShareContext("EthereumClassic", "ethereumclassic", "ethereum:0x1234"),
            new ShareContext("Digix", "ethereum:digix", "ethereum:0x1234"),
            new ShareContext("FirstBlood", "ethereum:firstblood", "ethereum:0x1234"),
            new ShareContext("Golem", "ethereum:golem", "ethereum:0x1234"),
            new ShareContext("HackerGold", "ethereum:hackergold", "ethereum:0x1234"),
            new ShareContext("ICONOMI", "ethereum:iconomi", "ethereum:0x1234"),
            new ShareContext("Maker", "ethereum:maker", "ethereum:0x1234"),
            new ShareContext("Pluton", "ethereum:pluton", "ethereum:0x1234"),
            new ShareContext("Augur", "ethereum:augur", "ethereum:0x1234"),
            new ShareContext("SingularDTV", "ethereum:singulardtv", "ethereum:0x1234"),
            new ShareContext("vDice", "ethereum:vdice", "ethereum:0x1234"),
    };

    /**
     * The contact ID.
     */
    public static final String ID = "context_id";

    /**
     * Representative invalid contact ID.
     */
    public static final int INVALID_ID = -1;

    /**
     * The name of this contact.
     */
    private final String mName;

    private final String mIdentifier;

    private final String mDomain;

    /**
     * Instantiates a new {@link ShareContext}.
     *
     * @param name The name of the contact.
     */
    public ShareContext(String name, String domain, String identifier) {
        mName = name;
        mDomain = domain;
        mIdentifier = identifier;
    }

    /**
     * Finds a {@link ShareContext} specified by a contact ID.
     *
     * @param id The contact ID. This needs to be a valid ID.
     * @return A {@link ShareContext}
     */
    public static ShareContext byId(int id) {
        return SHARE_CONTEXTS[id];
    }

    /**
     * Gets the name of this contact.
     *
     * @return The name of this contact.
     */
    public String getName() {
        return mName;
    }

    public String getIdentifier() {
        return mName;
    }

    public String getDomain() {
        return mDomain;
    }

    public String getSignatureType() {
        return "Ethereum.1";
    }

    /**
     * Gets the icon of this contact.
     *
     * @return The icon.
     */
    public int getIcon(Context context) {
        return context.getResources().getIdentifier(mName.toLowerCase(), "mipmap", context.getApplicationInfo().packageName);
    }

}
