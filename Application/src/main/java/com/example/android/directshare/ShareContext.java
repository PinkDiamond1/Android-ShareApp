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
            new ShareContext("Ethereum", "ethereum", "ethereum"),
            new ShareContext("Unicorn", "ethereum:unicorn", "ethereum:0x89205A3A3b2A69De6Dbf7f01ED13B2108B2c43e7"),
            new ShareContext("EthereumClassic", "ethereumclassic", "ethereumclassic"),
            new ShareContext("Digix DGD", "ethereum:digixdgd", "ethereum:0xe0b7927c4af23765cb51314a0e0521a9645f0e2a"),
            new ShareContext("FirstBlood", "ethereum:firstblood", "ethereum:0xAf30D2a7E90d7DC361c8C4585e9BB7D2F6f15bc7"),
            new ShareContext("Golem", "ethereum:golem", "ethereum:0xa74476443119A942dE498590Fe1f2454d7D4aC0d"),
            new ShareContext("HackerGold", "ethereum:hackergold", "ethereum:0x14f37b574242d366558db61f3335289a5035c506"),
            new ShareContext("ICONOMI", "ethereum:iconomi", "ethereum:0x888666CA69E0f178DED6D75b5726Cee99A87D698"),
            new ShareContext("Maker", "ethereum:maker", "ethereum:0xc66ea802717bfb9833400264dd12c2bceaa34a6d"),
            new ShareContext("Pluton", "ethereum:pluton", "ethereum:0xD8912C10681D8B21Fd3742244f44658dBA12264E"),
            new ShareContext("Augur", "ethereum:augur", "ethereum:0x48c80F1f4D53D5951e5D5438B54Cba84f29F32a5"),
            new ShareContext("SingularDTV", "ethereum:singulardtv", "ethereum:0xaec2e87e0a235266d9c5adc9deb4b2e29b54d009"),
            new ShareContext("vDice", "ethereum:vdice", "ethereum:0x5c543e7AE0A1104f78406C340E9C64FD9fCE5170"),
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
        return mIdentifier;
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
