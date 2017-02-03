package org.wso2.carbon.cluster.coordinator.zookeeper.test;

import org.wso2.carbon.cluster.coordinator.commons.MemberEventListener;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sameera on 1/23/17.
 */
public class EventListener extends MemberEventListener {

    public List<NodeDetail> memberAdded = new ArrayList<>();
    public List<NodeDetail> memberRemoved = new ArrayList<>();
    public List<NodeDetail> coordinatorChanged = new ArrayList<>();

    @Override public void memberAdded(NodeDetail nodeDetail) {
        this.memberAdded.add(nodeDetail);
    }

    @Override public void memberRemoved(NodeDetail nodeDetail) {
        this.memberRemoved.add(nodeDetail);
    }

    @Override public void coordinatorChanged(NodeDetail nodeDetail) {
        this.coordinatorChanged.add(nodeDetail);
    }
}
