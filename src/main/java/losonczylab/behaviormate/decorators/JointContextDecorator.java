package losonczylab.behaviormate.decorators;

import losonczylab.behaviormate.core.ContextList;
import losonczylab.behaviormate.core.ContextListDecorator;
import losonczylab.behaviormate.lists.JointContextList;
import org.json.JSONException;

/**
 * ?
 */
public class JointContextDecorator extends ContextListDecorator {
    /**
     * ?
     */
    protected JointContextList slave_list;

    /**
     * ?
     *
     * @param context_list <code>ContextList</code> instance the decorator will wrap.
     * @param slave_list ?
     */
    public JointContextDecorator(ContextList context_list, JointContextList slave_list) {
        super(context_list);
        this.slave_list = slave_list;
    }

    /**
     * Moves the context at the specified index to the given location on the track and
     * updates the instance's <code>JointContextList</code>.
     *
     * @param index The index of the context to move.
     * @param location The new location on the track in millimeters.
     */
    public void move(int index, int location) {
        context_list.move(index, location);
        slave_list.update();
    }

    /**
     * Randomizes the locations of the contexts in the wrapped <code>ContextList</code> and
     * updates the wrapped <code>JointContextList</code>.
     */
    public void shuffle() {
        context_list.shuffle();
        slave_list.update();
    }

    /**
     * Removes all contexts from the wrapped <code>ContextList</code> and updates the wrapped
     * <code>JointContextList</code>.
     */
    public void clear() {
        context_list.clear();
        slave_list.update();
    }

    /**
     * Resets the state of all contexts in the wrapped <code>ContextList</code> and updated the
     * wrapped <code>JointContextList</code>.
     */
    public void reset() {
        context_list.reset();
        slave_list.update();
    }
}
