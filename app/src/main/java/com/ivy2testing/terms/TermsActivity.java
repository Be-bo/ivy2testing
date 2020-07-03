package com.ivy2testing.terms;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;

import java.util.ArrayList;

public class TermsActivity extends AppCompatActivity {

    private RecyclerView text_recycler;
    private LongTextAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_terms);
        setTitle("Terms of Use");
        setUp();
    }

    private void setUp(){
        text_recycler = findViewById(R.id.terms_recycler);
        ArrayList<LongTextModel> allParagraphs = new ArrayList<LongTextModel>();
        allParagraphs.add(new LongTextModel("TERMS OF USE",""));
        allParagraphs.add(new LongTextModel("","These terms of use will be effective on September 5th, 2019."));
        allParagraphs.add(new LongTextModel("","By using the ivy social network app and ivy systems incorporated service you are agreeing to be bound by the following terms and conditions (“Terms of Use”)"));
        allParagraphs.add(new LongTextModel("Basic Terms","You must be 13 years or older to use this software.\n" +
                "You may not post nude, partially nude, or sexually suggestive photos or messages.\n" +
                "You are responsible for the activity that takes place under your account.\n" +
                "You are responsible for keeping your password secure\n" +
                "You must not abuse, harass, threaten, impersonate or intimidate other ivy users.\n" +
                "You may not use ivy services for any illegal or unauthorized purposes. International users agree to comply with all local laws regarding online conduct and acceptable content. \n" +
                "You are solely responsible for your conduct and any data, text, information, screen names, graphics, photos, profiles, audio and video clips, links (“Content”) that you as a user submit, post, and display on the ivy service.\n" +
                "You must not modify, hack, or adapt ivy or modify another website so as to falsely imply that it is associated with ivy. \n" +
                "You must not access ivy’s private API by any other means other than the ivy application itself. \n" +
                "You must not crawl, scrape, or otherwise cache any content from ivy, including but not limited to user profiles and photos.\n" +
                "You must not create or submit unwanted messages, email or comments to any ivy members (“Spam”).\n" +
                "You must not transmit any worms or viruses or any code of a destructive nature.\n" +
                "You must not, in the use of ivy, violate any laws in your jurisdiction (including but not limited to copyright laws or indecent exposure.)\n" +
                "Violation of any of these agreements will result in the termination of your ivy account. While ivy prohibits such conduct and content on its application, you understand and agree that ivy cannot be responsible for the Content posted on its web site and you nonetheless may be exposed to such materials and that you use the ivy service at your own risk."));
        allParagraphs.add(new LongTextModel("General Conditions","We reserve the right to modify or terminate the Ivy service for any reason, without notice at any time.\n" +
                "We reserve the right to alter these Terms of Use at any time. If the alterations constitute a material change to the Terms of Use, we will notify you via internet mail according to the preference expressed on your account. What constitutes a \"material change\" will be determined at our sole discretion, in good faith and using common sense and reasonable judgement.\n" +
                "We reserve the right to refuse service to anyone for any reason at any time.\n" +
                "We reserve the right to force forfeiture of any username that becomes inactive, violates trademark, or may mislead other users.\n" +
                "We may, but have no obligation to, remove Content and accounts containing Content that we determine in our sole discretion are unlawful, offensive, threatening, libelous, defamatory, obscene or otherwise objectionable or violates any party's intellectual property or these Terms of Use.\n" +
                "We reserve the right to reclaim usernames on behalf of businesses or individuals that hold legal claim or trademark on those usernames."));
        allParagraphs.add(new LongTextModel("Proprietary Rights in Content on ivy","ivy does NOT claim ANY ownership rights in the text, files, images, photos, video, sounds, musical works, works of authorship, applications, or any other materials (collectively, \"Content\") that you post on or through the ivy Systems incorporated Services. By displaying or publishing (\"posting\") any Content on or through the ivy Services, you hereby grant to ivy a non-exclusive, fully paid and royalty-free, worldwide, limited license to use, modify, delete from, add to, publicly perform, publicly display, reproduce and translate such Content, including without limitation distributing part or all of the application in any media formats through any media channels, except Content not shared publicly (\"private\") will not be distributed outside the ivy Services.\n" +
                "Some of the ivy Services may be supported by advertising revenue and may display advertisements and promotions, and you hereby agree that ivy may place such advertising and promotions on the ivy Platform or on, about, or in conjunction with your Content. The manner, mode and extent of such advertising and promotions are subject to change without specific notice to you.\n" +
                "You represent and warrant that: (i) you own the Content posted by you on or through the ivy platform or otherwise have the right to grant the license set forth in this section, (ii) the posting and use of your Content on or through the ivy platform does not violate the privacy rights, publicity rights, copyrights, contract rights, intellectual property rights or any other rights of any person, and (iii) the posting of your Content on the Site does not result in a breach of contract between you and a third party. You agree to pay for all royalties, fees, and any other monies owing any person by reason of Content you post on or through the Ivy Services.\n" +
                "The ivy platform contains Content of Ivy itself (\"Ivy Content\"). Ivy Content is protected by copyright, trademark, patent, trade secret and other laws, and ivy owns and retains all rights in the Ivy Content and the Ivy Services. Ivy hereby grants you a limited, revocable, non-sub licensable license to reproduce and display the Ivy Content (excluding any software code) solely for your personal use in connection with viewing the Site and using the Ivy Services.\n" +
                "The Ivy Services contain Content of Users and other Ivy licensors. Except as provided within this Agreement, you may not copy, modify, translate, publish, broadcast, transmit, distribute, perform, display, or sell any Content appearing on or through the Ivy Services.\n" +
                "Ivy performs technical functions necessary to offer the Ivy Services, including but not limited to transcoding and/or reformatting Content to allow its use throughout the Ivy Services.\n" +
                "Although the Site and other Ivy services are normally available, there will be occasions when the Site or other Ivy Services will be interrupted for scheduled maintenance or upgrades, for emergency repairs, or due to failure of telecommunications links and equipment that are beyond the control of Ivy. Also, although Ivy will normally only delete Content that violates this Agreement, Ivy reserves the right to delete any Content for any reason, without prior notice. Deleted content may be stored by Ivy in order to comply with certain legal obligations and is not retrievable without a valid court order. Consequently, Ivy encourages you to maintain your own backup of your Content. In other words, Ivy is not a backup service. Ivy will not be liable to you for any modification, suspension, or discontinuation of the ivy Services, or the loss of any Content."));
        allParagraphs.add(new LongTextModel("Intellectual Property and Ownership","ivy Systems Inc. shall at all times retain ownership of the Software as originally downloaded by you and all subsequent downloads of the Software by you. The Software (and the copyright, and other intellectual property rights of whatever nature in the Software, including any modifications made thereto) are and shall remain the property of ivy Systems Inc.\n" +
                "\n" +
                "ivy Systems Inc. reserves the right to grant licences to use the Software to third parties."));
        allParagraphs.add(new LongTextModel("Termination","This User agreement is effective from the date you first use the Software and shall continue until terminated. You may terminate it at any time upon written notice to ivy Systems Inc.\n" +
                "It will also terminate immediately if you fail to comply with any term of this user agreement. Upon such termination, the licenses granted by this agreement will immediately terminate and you agree to stop all access and use of the Software. The provisions that by their nature continue and survive will survive any termination of this agreement."));
        allParagraphs.add(new LongTextModel("Governing Law","This user agreement, and any dispute arising out of or in connection with this agreement, shall be governed by and construed in accordance with the laws of Canada."));


        adapter = new LongTextAdapter(this, allParagraphs);
        text_recycler.setLayoutManager(new LinearLayoutManager(this));
        text_recycler.setAdapter(adapter);
    }
}
