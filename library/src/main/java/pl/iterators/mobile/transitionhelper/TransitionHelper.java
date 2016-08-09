package pl.iterators.mobile.transitionhelper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.TransitionRes;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Karol Celebi on 05.08.2016.
 */

public class TransitionHelper {
    private static final String TRANSITION_ID = "transition_res_id";

    @SuppressWarnings("unchecked")
    private static Pair<View, String>[] createSafeTransitionParticipants(@NonNull Activity activity,
                                                                         boolean includeStatusBar, @Nullable Pair... otherParticipants) {
        // Avoid system UI glitches as described here:
        // https://plus.google.com/+AlexLockwood/posts/RPtwZ5nNebb
        View decor = activity.getWindow().getDecorView();
        View statusBar = null;
        View navBar = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (includeStatusBar) {
                statusBar = decor.findViewById(android.R.id.statusBarBackground);
            }
            navBar = decor.findViewById(android.R.id.navigationBarBackground);
        }

        // Create pair of transition participants.
        List<Pair> participants = new ArrayList<>(2);
        addNonNullViewToTransitionParticipants(statusBar, participants);
        addNonNullViewToTransitionParticipants(navBar, participants);
        // only add transition participants if there's at least one none-null element
        if (otherParticipants != null && !(otherParticipants.length == 1
                && otherParticipants[0] == null)) {
            participants.addAll(Arrays.asList(otherParticipants));
        }
        return participants.toArray(new Pair[participants.size()]);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void addNonNullViewToTransitionParticipants(View view, List<Pair> participants) {
        if (view == null || view instanceof Toolbar || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        participants.add(new Pair<>(view, view.getTransitionName()));
    }

    @SuppressWarnings("unchecked")
    public static void transitionTo(AppCompatActivity from, Intent intent, @TransitionRes int transition, View... sharedElements) {
        final Pair<View, String>[] pairs = createSafeTransitionParticipants(from, true, getPairs(sharedElements));
        ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(from, pairs);
        intent.putExtra(TRANSITION_ID, transition);
        from.startActivity(intent, transitionActivityOptions.toBundle());
    }

    @SuppressWarnings("unchecked")
    public static void transitionToForResult(AppCompatActivity from, Intent intent, int requestCode, @TransitionRes int transition, View... sharedElements) {
        final Pair<View, String>[] pairs = createSafeTransitionParticipants(from, true, getPairs(sharedElements));
        ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(from, pairs);
        intent.putExtra(TRANSITION_ID, transition);
        from.startActivityForResult(intent, requestCode, transitionActivityOptions.toBundle());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unchecked")
    private static Pair[] getPairs(View[] sharedElements) {
        if (sharedElements == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return null;
        List<Pair<View, String>> pairs = new ArrayList<>();

        for (View sharedElement : sharedElements) {
            if (sharedElement != null)
                pairs.add(Pair.create(sharedElement, sharedElement.getTransitionName()));
        }
        Pair<View, String>[] finalPairs = new Pair[pairs.size()];
        for (int i = 0; i < pairs.size(); i++) {
            finalPairs[i] = pairs.get(i);
        }
        return finalPairs;
    }

    public static void setupTransitions(AppCompatActivity appCompatActivity) {
        if (appCompatActivity.getIntent().hasExtra(TRANSITION_ID) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = TransitionInflater.from(appCompatActivity).inflateTransition(appCompatActivity.getIntent().getIntExtra(TRANSITION_ID, android.R.transition.no_transition));
            Transition fade = new Fade();
            fade.excludeTarget(android.R.id.navigationBarBackground, true);
            fade.excludeTarget(android.R.id.statusBarBackground, true);
            appCompatActivity.getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
            appCompatActivity.getWindow().setEnterTransition(fade);
            appCompatActivity.getWindow().setReturnTransition(fade);
            appCompatActivity.getWindow().setSharedElementsUseOverlay(true);
            appCompatActivity.getWindow().setSharedElementEnterTransition(transition);
            appCompatActivity.getWindow().setSharedElementReturnTransition(transition);
        }
    }
}