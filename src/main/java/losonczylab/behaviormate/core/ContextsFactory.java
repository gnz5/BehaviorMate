package losonczylab.behaviormate.core;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.util.PluginClassLoader;
import losonczylab.behaviormate.util.Str;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Entirely static class used to create a context list based on the "class" and "decorators"
 * attributes in the settings file.
 */
public final class ContextsFactory {

    private static Map<String, String> nameMapper;

    /**
     * @param context_info Contains the configuration information for this <code>ContextList</code> from the
     *                     settings file
     *
     * @return A new <code>ContextList</code> matching the parameters specified in <tt>context_info</tt>.
     */
    static ContextList Create(JSONObject context_info, String class_name) throws Exception {
        //System.out.println("ContextsFactory.Create(): context_info = " + context_info);

        setupNameMapper();
        ContextList cl = null;
        JSONArray decorators = null;

        if (!context_info.isNull(Str.DECORATORS)) {
            decorators = context_info.getJSONArray(Str.DECORATORS);
            context_info.remove(Str.DECORATORS);
        }

        if (class_name != null && !class_name.isBlank()) {
            class_name = class_name.toLowerCase();
            ClassLoader loader_class = new PluginClassLoader(new File(BehaviorMate.listsDir));
            class_name = "losonczylab.behaviormate.lists." + nameMapper.get(class_name);
            //System.out.println("ContextsFactory.Create(): class_name = " + class_name);
            Class c = loader_class.loadClass(nameMapper.get(class_name));
            Constructor<ContextList> con = c.getConstructor(JSONObject.class);
            cl = con.newInstance(context_info);
        }

        if (decorators != null) {
            for (int i=0; i < decorators.length(); i++) {
                JSONObject decorator = decorators.getJSONObject(i);
                //System.out.println("ContextFactor.create(): decorator = " + decorator);
                String decorator_name = decorator.getString("class").toLowerCase();
                ClassLoader loader_decorator = new PluginClassLoader(new File(BehaviorMate.decoratorsDir));
                if (nameMapper.get(decorator_name) == null) {
                    String message = "Invalid Context Decorator: " + decorator_name + "\n\n";
                    message += "Valid decorator names are: " + nameMapper.keySet();
                    throw new IllegalArgumentException(message);
                }
                decorator_name = "losonczylab.behaviormate.decorators." + nameMapper.get(decorator_name);
                Class c = loader_decorator.loadClass(decorator_name);
                Constructor<ContextList> con = c.getConstructor(ContextList.class, JSONObject.class);
                cl = new BasicContextList(context_info);
                cl = con.newInstance(cl, decorator);
                //System.out.println("ContextFactor.create(): cl = " + cl + ", context_info = " + context_info);
            }
        }
        return cl;
    }

    private static void setupNameMapper() {
        nameMapper = new HashMap<String, String>();
        nameMapper.put("vr", "VrContextList");
        nameMapper.put("vr_extended", "VrExtendedContextList");
        nameMapper.put("vr_cue", "VrCueContextList");
        nameMapper.put("salience", "SalienceContextList");
        nameMapper.put("paired_reward_stim", "PairedRewardStimContextList");
        nameMapper.put("gain_mod", "GainModifiedContextList");
        nameMapper.put("fog_context", "VrFogContext");
        nameMapper.put("joint_context", "JointContextList");
        nameMapper.put("alternating", "AlternatingContextDecorator");
        nameMapper.put("blockedshuffle", "BlockedShuffleContextDecorator");
        nameMapper.put("delayed", "DelayedContextDecorator");
        nameMapper.put("gainmodified", "GainModifiedContextDecorator");
        nameMapper.put("joint", "JointContextDecorator");
        nameMapper.put("lickstart", "LickStartContextDecorator");
        nameMapper.put("pairedrewardstim", "PairedRewardStimContextDecorator");
        nameMapper.put("random", "RandomContextDecorator");
        nameMapper.put("running", "RunningContextDecorator");
        nameMapper.put("scheduled", "ScheduledContextDecorator");
        nameMapper.put("timed", "TimedContextDecorator");
        nameMapper.put("timediti", "TimedITIContextDecorator");
    }
}
