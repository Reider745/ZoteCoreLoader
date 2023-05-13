package com.zhekasmirnov.innercore.api.mod;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.annotations.JSFunction;

import java.util.*;

public class TagRegistry {
    public interface TagFactory {
        void addTags(Object obj, Collection<String> tags);
    }

    public interface TagPredicate {
        boolean check(Object obj, Collection<String> tags);
    }
    
    public static class TagGroup {
        public final String name;

        private final HashMap<String, HashSet<String>> primaryTagMap = new HashMap<>();
        private final List<TagFactory> tagFactories = new ArrayList<>();

        private boolean isCommonObjectsCollectionDirty = false;
        private final HashMap<Object, HashSet<String>> commonObjectCollection = new HashMap<>();

        public TagGroup(String name) {
            this.name = name;
        }

        public void addTagFactory(TagFactory factory) {
            if (factory != null) {
                tagFactories.add(factory);
                isCommonObjectsCollectionDirty = true;
            }
        }

        public void addTagsFor(Object obj, String... tags) {
            synchronized(commonObjectCollection) {
                String key = obj != null ? obj.toString() : null;
                HashSet<String> tagSet = primaryTagMap.get(key);
                if (tagSet == null) {
                    tagSet = new HashSet<>();
                    primaryTagMap.put(key, tagSet);
                }
                for (String tag : tags) {
                    tagSet.add(tag);   
                }
                isCommonObjectsCollectionDirty = true;
            }
        }
        
        public void removeTagsFor(Object obj, String... tags) {
            synchronized(commonObjectCollection) {
                String key = obj != null ? obj.toString() : null;
                HashSet<String> tagSet = primaryTagMap.get(key);
                if (tagSet != null) {
                    for (String tag : tags) {
                        tagSet.remove(tag);   
                    }
                }
                isCommonObjectsCollectionDirty = true;
            }
        }

        public void addCommonObject(Object obj, String... tags) {
            addTagsFor(obj, tags);
            synchronized(commonObjectCollection) {
                commonObjectCollection.put(obj, getTags(obj));
            }
        }

        public void removeCommonObject(Object obj) {
            synchronized(commonObjectCollection) {
                commonObjectCollection.remove(obj);
            }
        }

        public void addTags(Object obj, Collection<String> tags) {
            HashSet<String> primaryTags = primaryTagMap.get(obj != null ? obj.toString() : null);
            if (primaryTags != null) {
                tags.addAll(primaryTags);
            }
            if (obj instanceof Scriptable) {
                ScriptableObjectWrapper wrapper = new ScriptableObjectWrapper((Scriptable) obj);
                if (wrapper.has("_tags")) {
                    ScriptableObjectWrapper tagsArr = wrapper.getScriptableWrapper("_tags");
                    if (tagsArr != null) {
                        for (Object tag : tagsArr.asArray()) {
                            if (tag != null) {
                                tags.add(tag.toString());
                            }
                        }
                    }
                }
            }
            for (TagFactory factory : tagFactories) {
                factory.addTags(obj, tags);
            }
        }

        public HashSet<String> getTags(Object obj) {
            HashSet<String> tags = new HashSet<>();
            addTags(obj, tags);
            return tags;
        }

        public List<Object> getAllWhere(TagPredicate predicate) {
            List<Object> result = new ArrayList<>();
            synchronized(commonObjectCollection) {
                boolean isDirty = isCommonObjectsCollectionDirty;
                isCommonObjectsCollectionDirty = false;
                for (Map.Entry<Object, HashSet<String>> entry : commonObjectCollection.entrySet()) {
                    if (isDirty) {
                        entry.setValue(getTags(entry.getKey()));
                    }
                    if (predicate.check(entry.getKey(), entry.getValue())) {
                        result.add(entry.getKey());
                    }
                }
            }
            return result;
        } 

        public List<Object> getAllWithTags(final Collection<String> checkTags) {
            return getAllWhere(new TagPredicate() {
                public boolean check(Object obj, Collection<String> tags) {
                    for (String tag : checkTags) {
                        if (!tags.contains(tag)) {
                            return false;
                        }
                    }
                    return true;
                }
            });  
        }

        public List<Object> getAllWithTag(final String tag) {
            return getAllWhere(new TagPredicate() {
                public boolean check(Object obj, Collection<String> tags) {
                    return tags.contains(tag);
                }
            });  
        }
    }

    private static final HashMap<String, TagGroup> groups = new HashMap<>();

    @JSFunction
    public static TagGroup getOrCreateGroup(String name) {
        if (groups.containsKey(name)) {
            return groups.get(name);
        } else {
            TagGroup group = new TagGroup(name);
            groups.put(name, group);
            return group;
        }
    }
    
}