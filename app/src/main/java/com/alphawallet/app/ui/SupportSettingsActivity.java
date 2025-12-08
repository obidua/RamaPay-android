package com.alphawallet.app.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.analytics.Analytics;
import com.alphawallet.app.entity.MediaLinks;
import com.alphawallet.app.viewmodel.SupportSettingsViewModel;
import com.alphawallet.app.widget.SettingsItemView;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class SupportSettingsActivity extends BaseActivity
{
    private SupportSettingsViewModel viewModel;
    private LinearLayout supportSettingsLayout;
    private SettingsItemView discord;
    private SettingsItemView email;
    private SettingsItemView twitter;
    private SettingsItemView reddit;
    private SettingsItemView facebook;
    private SettingsItemView instagram;
    private SettingsItemView blog;
    private SettingsItemView faq;
    private SettingsItemView github;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_settings);

        toolbar();

        setTitle(getString(R.string.title_support));

        initViewModel();

        initializeSettings();

        addSettingsToLayout();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        viewModel.track(Analytics.Navigation.SETTINGS_SUPPORT);
    }

    private void initViewModel()
    {
        viewModel = new ViewModelProvider(this).get(SupportSettingsViewModel.class);
    }

    private void initializeSettings()
    {
        discord = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_logo_discord)
                .withTitle(R.string.discord)
                .withListener(this::onDiscordClicked)
                .build();

        email = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_email)
                .withTitle(R.string.email)
                .withListener(this::onEmailClicked)
                .build();

        twitter = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_logo_twitter)
                .withTitle(R.string.twitter)
                .withListener(this::onTwitterClicked)
                .build();

        facebook = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_logo_facebook)
                .withTitle(R.string.facebook)
                .withListener(this::onFacebookClicked)
                .build();

        instagram = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_logo_instagram)
                .withTitle(R.string.instagram)
                .withListener(this::onInstagramClicked)
                .build();

        blog = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_settings_blog)
                .withTitle(R.string.title_blog)
                .withListener(this::onBlogClicked)
                .build();

        github = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_logo_github)
                .withTitle(R.string.github)
                .withListener(this::onGitHubClicked)
                .build();

        faq = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_settings_faq)
                .withTitle(R.string.title_faq)
                .withListener(this::onFaqClicked)
                .build();
    }

    private void addSettingsToLayout()
    {
        supportSettingsLayout = findViewById(R.id.layout);

        if (MediaLinks.AWALLET_DISCORD_URL != null)
        {
            supportSettingsLayout.addView(discord);
        }

        if (MediaLinks.AWALLET_EMAIL1 != null)
        {
            supportSettingsLayout.addView(email);
        }

        if (MediaLinks.AWALLET_TWITTER_URL != null)
        {
            supportSettingsLayout.addView(twitter);
        }

        if (MediaLinks.AWALLET_FACEBOOK_URL != null)
        {
            supportSettingsLayout.addView(facebook);
        }

        if (MediaLinks.AWALLET_INSTAGRAM_URL != null)
        {
            supportSettingsLayout.addView(instagram);
        }

        if (MediaLinks.AWALLET_GITHUB != null)
        {
            supportSettingsLayout.addView(github);
        }

        if (MediaLinks.AWALLET_BLOG_URL != null)
        {
            supportSettingsLayout.addView(blog);
        }

        supportSettingsLayout.addView(faq);
    }

    private void onGitHubClicked()
    {
        Intent intent = new Intent();
        intent.putExtra(C.DAPP_URL_LOAD, MediaLinks.AWALLET_GITHUB);
        viewModel.track(Analytics.Action.SUPPORT_GITHUB);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onDiscordClicked()
    {
        if (MediaLinks.isMediaTargeted(getApplicationContext()))
        {
            try
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MediaLinks.AWALLET_DISCORD_URL));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                viewModel.track(Analytics.Action.SUPPORT_DISCORD);
                startActivity(intent);
            }
            catch (Exception e)
            {
                Timber.e(e);
            }
        }
    }

    private void onEmailClicked()
    {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        final String at = "@";
        String email =
                "mailto:" + MediaLinks.AWALLET_EMAIL1 + at + MediaLinks.AWALLET_EMAIL2 +
                        "?subject=" + Uri.encode(MediaLinks.AWALLET_SUBJECT) +
                        "&body=" + Uri.encode("");
        intent.setData(Uri.parse(email));

        try
        {
            viewModel.track(Analytics.Action.SUPPORT_EMAIL);
            startActivity(intent);
        }
        catch (Exception e)
        {
            Timber.e(e);
        }
    }

    private void onLinkedInClicked()
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(MediaLinks.AWALLET_LINKEDIN_URL));
        if (isAppAvailable(C.LINKEDIN_PACKAGE_NAME))
        {
            intent.setPackage(C.LINKEDIN_PACKAGE_NAME);
        }
        try
        {
            startActivity(intent);
        }
        catch (Exception e)
        {
            Timber.e(e);
        }
    }

    private void onTwitterClicked()
    {
        Intent intent = new Intent();
        intent.putExtra(C.DAPP_URL_LOAD, MediaLinks.AWALLET_TWITTER_URL);
        viewModel.track(Analytics.Action.SUPPORT_TWITTER);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onRedditClicked()
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (isAppAvailable(C.REDDIT_PACKAGE_NAME))
        {
            intent.setPackage(C.REDDIT_PACKAGE_NAME);
        }

        intent.setData(Uri.parse(MediaLinks.AWALLET_REDDIT_URL));

        try
        {
            startActivity(intent);
        }
        catch (Exception e)
        {
            Timber.e(e);
        }
    }

    private void onFacebookClicked()
    {
        Intent intent = new Intent();
        intent.putExtra(C.DAPP_URL_LOAD, MediaLinks.AWALLET_FACEBOOK_URL);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onInstagramClicked()
    {
        Intent intent = new Intent();
        intent.putExtra(C.DAPP_URL_LOAD, MediaLinks.AWALLET_INSTAGRAM_URL);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onBlogClicked()
    {
        Intent intent = new Intent();
        intent.putExtra(C.DAPP_URL_LOAD, MediaLinks.AWALLET_BLOG_URL);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onFaqClicked()
    {
        Intent intent = new Intent();
        intent.putExtra(C.DAPP_URL_LOAD, MediaLinks.AWALLET_FAQ_URL);
        viewModel.track(Analytics.Action.SUPPORT_FAQ);
        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean isAppAvailable(String packageName)
    {
        PackageManager pm = getPackageManager();
        try
        {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
