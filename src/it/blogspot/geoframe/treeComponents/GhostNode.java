/*
 * GNU GPL v3 License
 *
 * Copyright 2015 AboutHydrology (Riccardo Rigon)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.blogspot.geoframe.treeComponents;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.TreeTraverser;

import it.blogspot.geoframe.Connections;
import it.blogspot.geoframe.hydroGeoEntities.HydroGeoEntity;
import it.blogspot.geoframe.hydroGeoEntities.point.HydroGeoPoint;
import it.blogspot.geoframe.key.Key;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * @brief class Ghost Node
 *
 * @description The main purpose of this class is the representation of
 *              <tt>ghost nodes</tt> inside the binary tree designed with the
 *              <strong>Composite Pattern</strong>. <tt>ghost node</tt> are
 *              necessary because a river network might have a intersection with
 *              more than two streams and using <strong>binary tree</strong> as
 *              schema doesn't allow that. In an intersection with 3 streams, a
 *              <tt>ghost node</tt> enables outline it with a stream and the
 *              <tt>ghost node</tt> where the other two streams flows
 *              <p>
 *              This class is <em>ThreadSafe</em> because:
 *              <ul>
 *              <li>Each state is guarded by the <strong>intrinsic lock</strong>
 *              ;</li>
 *              <li>Each method is <strong>synchronized</strong> in order to
 *              deny stale data if a two threads simultaneously call setter and
 *              getter methods;</li>
 *              <li>The <strong>invariant</strong> is ensured by the method
 *              GhostNode#setNewKey(final Key) and checked by the method
 *              Component#validateInvariant(final Key, final Key, final Key, final Key).</li>
 *              </ul>
 *              </p>
 *
 * @author sidereus, francesco.serafin.3@gmail.com
 * @version 0.1
 * @date October 13, 2015
 * @copyright GNU Public License v3 AboutHydrology (Riccardo Rigon)
 */
@ThreadSafe
public class GhostNode extends Component {

    @GuardedBy("this") private Connections connKeys; //!< connections of the node
    @GuardedBy("this") private HydroGeoEntity entity; //!<
    @GuardedBy("this") private TreeTraverser<Component> traverser; //!< traverser object
    @GuardedBy("this") private final HashMap<Key, Boolean> readyForSim
        = new HashMap<Key, Boolean>(); //!< <code>HashMap</code> of flags for start sim

    /**
     * @brief Constructor
     *
     * @param[in] connKeys The connection of the node
     * @param[in] entity The type of entity this node is going to be
     */
    public GhostNode(final Connections connKeys, final HydroGeoEntity entity) {
        getInstance(connKeys, entity);
    }

    /**
     * @brief <tt>notify</tt> method from <strong>Observer Pattern</strong>
     *
     * @description This method is used by children to notify to the parent that
     *              the computation of their simulation is finished.
     *
     * @param[in] child The key of the child whose computation is finished
     */
    @Override
    public synchronized void notify(final Key child) {
        readyForSim.replace(child, true);
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#isReadyForSimulation()
     */
    public synchronized boolean isReadyForSimulation() {
        return (!readyForSim.values().contains(false)) ? true : false;
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#runSimulation(final Component)
     */
    public synchronized void runSimulation(final Component parent) {
        if (!parent.getConnections().getPARENT().equals(connKeys.getPARENT()))
            throw new IllegalArgumentException("Node not connected with parent");

        try {
            String message = this.getClass().getSimpleName();
            message += "  " + connKeys.getID().getDouble();
            message += " ==> " + Thread.currentThread().getName();
            message += " Computing..." + " PARENT = ";
            message += parent.getConnections().getPARENT().getDouble();
            System.out.println(message);
            Thread.sleep(5000); // lock is hold
        } catch (InterruptedException e) {}

        parent.notify(connKeys.getID());
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#setNewConnections(final Connections)
     */
    public synchronized void setNewConnections(final Connections connKeys) {
        validateConnections(connKeys); // precondition
        this.connKeys = connKeys;
        allocateSimulationFlags(); // update of the flags for the simulation
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#getConnections()
     */
    public synchronized Connections getConnections() {
        return connKeys;
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#getStartPoint()
     */
    public synchronized HydroGeoPoint getStartPoint() {
        return entity.getStartPoint();
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#getEndPoint()
     */
    public synchronized HydroGeoPoint getEndPoint() {
        return entity.getEndPoint();
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#getEntity()
     */
    public synchronized HydroGeoEntity getEntity() {
        return entity;
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#setTraverser(final BinaryTreeTraverser<Component>)
     */
    public synchronized void setTraverser(final TreeTraverser<Component> traverser) {
        if (traverser == null) throw new NullPointerException("Traverser cannot be null."); // precondition
        this.traverser = traverser;
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#preOrderTraversal()
     */
    public synchronized List<Component> preOrderTraversal() {
        FluentIterable<Component> iterator = traverser.preOrderTraversal(this);
        return iterator.toList();
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#postOrderTraversal()
     */
    public synchronized List<Component> postOrderTraversal() {
        FluentIterable<Component> iterator = traverser.postOrderTraversal(this);
        return iterator.toList();
    }

    /**
     * @brief Simply overriding of the <code>toString</code> method
     *
     * @return The state variables of the object
     */
    @Override
    public String toString() {
  
        String tmp = this.getClass().getSimpleName();
        tmp += "  ==> ";
        tmp += connKeys.toString();

        return tmp;

    }

    /**
     * @brief Method that follows the rules of the <strong>Singleton
     * Pattern</strong> @cite freeman2004:head
     *
     * @description Double-checked locking
     *
     * @param[in] connKeys The connections of the node
     * @param[in] entity The type of entity the node is going to be
     */
    private void getInstance(final Connections connKeys, final HydroGeoEntity entity) {

        if (statesAreNull()) {
            synchronized(this) {
                if (statesAreNull()) {
                    this.connKeys = connKeys;
                    this.entity = entity;

                    validateState(); // precondition
                    allocateSimulationFlags();
                }

            }

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see Component#statesAreNull()
     */
    protected boolean statesAreNull() {

        if (this.connKeys == null &&
            this.entity == null) return true;

        return false;

    }

    /**
     * {@inheritDoc}
     *
     * @see Component#validateState()
     */
    protected void validateState() {

        validateConnections(connKeys);

    }

    /**
     * {@inheritDoc}
     *
     * @see Component#allocateSimulationFlags()
     */
    protected void allocateSimulationFlags() {
        readyForSim.clear();

        if (connKeys.getNumberNonNullChildren() != 0) {
            for (Key childKey : connKeys.getChildren())
                readyForSim.putIfAbsent(childKey, false);
        } else {
            String message = this.getClass().getSimpleName();
            message += " has no children. This is not allowed,";
            message += " only Leaf node can have no children.";
            throw new NullPointerException(message);
        }

    }

}
