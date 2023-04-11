//package losonczylab.behaviormate;
//
//import java.util.ArrayList;
//
///**
// * ?
// */
//public class JointSuspendContextDecorator extends SuspendableContextDecorator {
//
//    /**
//     *
//     */
//    protected String joint_list_id;
//
//    /**
//     *
//     */
//    protected boolean master;
//
//    /**
//     *
//     */
//    protected ArrayList<SuspendableContextDecorator> master_suspendables;
//
//    /**
//     * ?
//     *
//     * @param context_list <code>ContextList</code> instance the decorator will wrap.
//     * @param context_info JSONObject containing the configuration information for this context
//     *                     from the settings file. <tt>context_info</tt> should have the parameter
//     *                     <tt>joint_id</tt> set to do ?. The <tt>master</tt> parameter is optional
//     *                     and will default to false if not provided.
//     */
//    public JointSuspendContextDecorator(ContextList context_list, JSONObject context_info) {
//        super(context_list);
//        joint_list_id = context_info.getString("joint_id");
//        master = context_info.getBoolean("master", false);
//    }
//
//    /**
//     * ?
//     *
//     * @param contexts ?
//     */
//    public void registerContexts(ArrayList<ContextList> contexts) {
//        ContextList joint_list = null;
//        for (ContextList context_list : contexts) {
//            if (context_list.getId().equals(joint_list_id)) {
//                joint_list = context_list;
//
//                String other_id = "";
//                while (true) {
//                    try {
//                        other_id = ((JointSuspendContextDecorator) context_list).joint_id();
//                    } catch (Exception e) { }
//
//                    if (other_id.equals(getId())) {
//                        break;
//                    }
//
//                    try {
//                        context_list = ((ContextListDecorator) context_list).getContextListBase();
//                    } catch (ClassCastException e) {
//                        throw new IllegalArgumentException("Joint List [" + joint_list_id + "] Not Found");
//                    }
//                }
//                break;
//            }
//        }
//
//        master_suspendables = new ArrayList<>();
//        ContextListDecorator cl = (ContextListDecorator) joint_list;
//        while (true) {
//            SuspendableContextDecorator sus_context = null;
//            try {
//                sus_context = ((SuspendableContextDecorator) cl);
//            } catch (Exception e) { }
//
//            if (sus_context != null) {
//                master_suspendables.add(sus_context);
//            }
//
//            try {
//                cl = (ContextListDecorator) cl.getContextListBase();
//            } catch (ClassCastException e) {
//                break;
//            }
//        }
//    }
//
//    /**
//     * Suspend all contexts belonging to the wrapped <code>ContextList</code> (inherited from
//     * <code>ContextListDecorator</code>).
//     */
//    public void suspend() {
//        super.suspend();
//    }
//
//    /**
//     *
//     * @return <code>true</code> if all wrapped <code>SuspendableContextDecorator</code> objects
//     * are suspended, <code>false</code> otherwise.
//     */
//    protected boolean masterSuspended() {
//        for (SuspendableContextDecorator sus_context : master_suspendables) {
//            if (sus_context.isSuspended()) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    /**
//     *
//     * @return ?
//     */
//    public String joint_id() {
//        return joint_list_id;
//    }
//
//    /**
//     * Check the state of the list as well as the contexts contained in this and decide if they
//     * should be activated or not. Send the start/stop messages as necessary. this method gets
//     * called for each cycle of the event loop when a trial is started.
//     *
//     * @return           ?
//     */
//    public boolean check_suspend() {
//
//        if (!master) {
//            return masterSuspended();
//        } else {
//            return false;
//        }
//    }
//}