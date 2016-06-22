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

import java.util.List;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.TreeTraverser;

import it.blogspot.geoframe.Connections;
import it.blogspot.geoframe.hydroGeoEntities.HydroGeoEntity;
import it.blogspot.geoframe.hydroGeoEntities.point.HydroGeoPoint;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * @brief class Leaf
 *
 * @description The main purpose of this class is the representation of
 *              subbasins that don't have children.
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
public class Leaf extends Component {

    @GuardedBy("this") private Connections connKeys; //!< connections of the node
    @GuardedBy("this") private HydroGeoEntity entity; //!<
    @GuardedBy("this") private TreeTraverser<Component> traverser; //!< traverser object

    /**
     * @brief Constructor
     *
     * @param[in] connKeys The connection of the node
     * @param[in] entity The entity of the node
     */
    public Leaf(final Connections connKeys, final HydroGeoEntity entity) {
        getInstance(connKeys, entity);
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#isReadyForSimulation()
     */
    public synchronized boolean isReadyForSimulation() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#runSimulation(final Component)
     */
    public synchronized void runSimulation(final Component parent) {
        if (!parent.getConnections().getID().equals(connKeys.getPARENT()))
            throw new IllegalArgumentException("Node not connected with parent");

        try {
            String message = this.getClass().getSimpleName();
            message += "       " + connKeys.getID().getDouble();
            message += " ==> " + Thread.currentThread().getName();
            message += " Computing..." + " PARENT = ";
            message += connKeys.getPARENT().getDouble();
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
        tmp += "       ==> ";
        tmp += connKeys.toString();

        return tmp;

    }

    /**
     * @brief Allocation of the states of the class
     *
     * @param[in] connKeys The connections of the node
     * @param[in] entity The entity of node
     */
    private void getInstance(final Connections connKeys, final HydroGeoEntity entity) {

        if (statesAreNull()) {
            synchronized(this) {
                if (statesAreNull()) {
                    this.connKeys = connKeys;
                    this.entity = entity;

                    validateState(); // precondition
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

    protected void allocateSimulationFlags() {
        // nothing to implement here. Leaf has no simulation flags
    }

}
