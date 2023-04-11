//package losonczylab.behaviormate.decorators;
//
//import losonczylab.behaviormate.core.ContextList;
//import losonczylab.behaviormate.core.ContextListDecorator;
//import org.json.JSONObject;
//import java.util.ArrayList;
//
///**
// *
// */
//public final class JointContextDecorator extends ContextListDecorator {
//    private ArrayList<ContextList> slaves;
//
//    // used to alternate been two or more slave contexts
//    private final class alternating_slaves extends ContextListDecorator {
//        private ArrayList<ContextList> slaves;
//
//        private alternating_slaves(ContextList context_list, JSONObject context_info) {
//            super(context_list);
//        }
//    }
//
//    // used to randomly unsuspend one of the two or more slave contexts
//    private final class random_slaves extends ContextListDecorator {
//        private ArrayList<ContextList> slaves;
//
//        private random_slaves(ContextList context_list, JSONObject context_info) {
//            super(context_list);
//        }
//    }
//
//    private ContextList master;
//
//
//    public JointContextDecorator(ContextList context_list, JSONObject context_info) {
//        super(context_list);
//
//    }
//}