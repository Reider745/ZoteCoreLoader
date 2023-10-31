package cn.nukkit.entity.ai.behavior;

import cn.nukkit.api.PowerNukkitXOnly;
import cn.nukkit.api.Since;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.behaviorgroup.IBehaviorGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 由多个行为{@link IBehavior}组成的组（注意和行为组{@link IBehaviorGroup}区分）<br>
 * 调用方法{@link #execute(EntityIntelligent)}前，必须调用此对象的评估函数以确认激活的是哪个行为<br>
 * 在评估时，会评估所有包含的子行为<br>
 * 筛选出返回成功的行为后，会选取最高优先级的那一组<br>
 * 如果到这一步依然存在多个行为，则会根据行为的{@link IBehavior#getWeight()}方法的返回值随机选取其中一个作为执行行为
 * <p>
 * A group consisting of multiple behaviors {@link IBehavior} (note the distinction with behavior groups {@link IBehaviorGroup})<br>
 * Before calling the method {@link #execute(EntityIntelligent)}, the evaluation function of this object must be called to confirm which behavior is activated<br>
 * During evaluation, all contained child behaviors are evaluated<br>
 * After filtering out the behaviors that return success, the group with the highest priority is selected<br>
 * If there are still multiple behaviors at this point, one of them is randomly selected for execution based on the return value of the {@link IBehavior#getWeight()} method of the behavior
 */
@PowerNukkitXOnly
@Since("1.6.0.0-PNX")
@Getter
public class WeightedMultiBehavior extends AbstractBehavior {
    /**
     * 此组的优先级。在BehaviorGroup中，获取优先级将会返回此值指代整个组的优先级
     * <p>
     * The priority of this group. In Behavior Group, getting the priority will return this value to refer to the priority of the entire group
     */
    protected final int priority;
    protected Set<IBehavior> behaviors;
    @Setter
    protected IBehavior currentBehavior;

    public WeightedMultiBehavior(int priority, IBehavior... behaviors) {
        this.priority = priority;
        this.behaviors = Set.of(behaviors);
    }

    public WeightedMultiBehavior(int priority, Set<IBehavior> behaviors) {
        this.priority = priority;
        this.behaviors = behaviors;
    }

    @Override
    public boolean evaluate(EntityIntelligent entity) {
        var result = evaluateBehaviors(entity);
        if (result.isEmpty()) {
            return false;
        }
        if (result.size() == 1) {
            setCurrentBehavior(result.iterator().next());
            return true;
        }
        //根据Weight选取一个行为
        int totalWeight = 0;
        for (IBehavior behavior : result) {
            totalWeight += behavior.getWeight();
        }
        int random = ThreadLocalRandom.current().nextInt(totalWeight + 1);
        for (IBehavior behavior : result) {
            random -= behavior.getWeight();
            if (random <= 0) {
                setCurrentBehavior(behavior);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        if (currentBehavior == null) {
            return false;
        }
        return currentBehavior.execute(entity);
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        if (currentBehavior == null) {
            return;
        }
        currentBehavior.onInterrupt(entity);
        currentBehavior.setBehaviorState(BehaviorState.STOP);
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        if (currentBehavior == null) {
            return;
        }
        currentBehavior.onStart(entity);
        currentBehavior.setBehaviorState(BehaviorState.ACTIVE);
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        if (currentBehavior == null) {
            return;
        }
        currentBehavior.onStop(entity);
        currentBehavior.setBehaviorState(BehaviorState.STOP);
    }

    /**
     * @param entity 实体
     * @return 最高优先级且评估成功的一组行为（包含评估结果）
     */
    protected Set<IBehavior> evaluateBehaviors(EntityIntelligent entity) {
        //存储评估成功的行为（未过滤优先级）
        var evalSucceed = new HashSet<IBehavior>();
        int highestPriority = Integer.MIN_VALUE;
        for (IBehavior behavior : behaviors) {
            if (behavior.evaluate(entity)) {
                evalSucceed.add(behavior);
                if (behavior.getPriority() > highestPriority) {
                    highestPriority = behavior.getPriority();
                }
            }
        }
        //如果没有评估结果，则返回空
        if (evalSucceed.isEmpty()) {
            return evalSucceed;
        }
        //过滤掉低优先级的行为
        var result = new HashSet<IBehavior>();
        for (IBehavior entry : evalSucceed) {
            if (entry.getPriority() == highestPriority) {
                result.add(entry);
            }
        }
        return result;
    }
}
