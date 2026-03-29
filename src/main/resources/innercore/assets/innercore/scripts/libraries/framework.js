/*
BUILD INFO:
  dir: framework-dev
  target: lib/framework.js
  files: 6
*/


// file: header.js

LIBRARY({
    name: "framework",
    version: 1,
    shared: true,

    api: "CoreEngine"
});


// file: class/helper.js

/* clones prototype into given object, used to instantiate and clone classes */
function instantiateTo(prototype, instance) {
    for (var name in prototype) {
        if (!String(name).startsWith("$")) {
            var val = prototype[name];
            if (typeof (val) == "object") {
                if (val.__clone__) {
                    val = val.__clone__();
                } else {
                    val = instantiateTo(val, Array.isArray(val) ? [] : {});
                }
            }
            instance[name] = val;
        }
    }

    return instance;
}


// file: class/baseclass.js


var BaseClass = {
    getClassPrototype: function () {
        return {
            __init__: function () {

            },

            __load__: function () {

            },

            __hash__: function () {
                return this.$serialId;
            },

            __clone__: function () {
                return instantiateTo(this, {});
            },

            __serialize__: function () {
                var serialized = {};
                for (var name in this) {
                    if (!name.startsWith("$") && !name.startsWith("_$") && name != "super") {
                        var val = this[name];
                        if (typeof (val) != "function") {
                            serialized[name] = val;
                        }
                    }
                }

                return serialized;
            },

            __deserialize__: function (data) {
                for (var name in data) {
                    this[name] = data[name];
                }
            },

            toString: function () {
                return this.getClass().getName() + "@" + this.__hash__();
            }
        };
    },

    isDirectInstance: function (instance) {
        return false;
    },

    isInstance: function (instance) {
        return instance && instance.getClass;
    },

    getSuperClass: function () {
        return null;
    },

    getName: function () {
        return "BaseClass";
    },

    getScopeName: function () {
        return "Framework";
    },

    getFullName: function () {
        return "Framework$BaseClass";
    },

    toString: function () {
        return "class " + this.getName();
    }
};


// file: class/superclass.js

/**
 represents "super" field of the class instance to access parent methods

 receives superclass prototype, returns function, that can be called for class instance to receive "super" field for it.

 */
function SuperClass(prototype) {
    var $super = {};
    var names = {};
    for (var name in prototype) {
        if (name.startsWith("_$") || typeof (prototype[name]) == "function") {
            names[name] = true;
            $super[name] = prototype[name];
        }
    }

    return function (scope) {
        var supreme = null;
        if (prototype.$super) {
            supreme = prototype.$super(scope);
        }

        return new org.mozilla.javascript.ScriptableObject()
        {
            getClassName: function () {
                return "super";
            }
        ,

            getDefaultValue: function () {
                return scope;
            }
        ,

            get: function (key, scriptable) {
                if (key == "super") {
                    return supreme;
                }
                return names[key] ? $super[key] : scope[key];
            }
        ,

            put: function (key, scriptable, value) {
                if (!names[key]) {
                    scope[key] = value;
                }
            }
        }
        ;
    }
}


// file: class/saver.js

var ClassLinker = {
    saverId: 1,

    saved: {},
    linked: {},

    reset: function () {
        this.saverId = 1;
        this.linked = {};
        this.saved = {};
    },

    getLink: function (id) {
        var link;
        if (this.linked.hasOwnProperty(id)) {
            link = this.linked[id];
        } else {
            link = {};
            this.linked[id] = link;
        }
        return link;
    },

    genSaverId: function (instance) {
        if (!this.isSaved(instance)) {
            instance.$saverId = this.saverId++;
            this.saved[instance.$saverId] = true;
        }
        return instance.$saverId;
    },

    isSaved: function (instance) {
        return this.saved[instance.$saverId];
    }
};

Callback.addCallback("WriteSaves", function () {
    ClassLinker.reset();
});

Callback.addCallback("ReadSaves", function () {
    ClassLinker.reset();
});


function ClassSaver(clazz) {
    return Saver.registerObjectSaver(clazz.getFullName(), {
        read: function (data) {
            if (data.$link) {
                return ClassLinker.getLink(data.$link);
            } else {
                var instance = clazz.instantiateTo(ClassLinker.getLink(data.$saverId));
                instance.__deserialize__(data);
                instance.__load__();
                return instance;
            }
        },

        save: function (instance) {
            if (ClassLinker.isSaved(instance)) {
                return {$link: instance.$saverId};
            } else {
                var serialized = instance.__serialize__();
                serialized.$saverId = ClassLinker.genSaverId(instance);
                return serialized;
            }
        }
    });
}

var AnonymousClassSaver = Saver.registerObjectSaver("framework.runtime.cla$$", {
    save: function () {
        return null;
    },

    read: function () {
        return null;
    }
});


// file: class/$.js


/*
 Represents main framework operator - class creator

 var SampleClass = $("SampleClass", {
    extends: AnotherClass,
    includes: [
        SomeClass1,
        SomeClass2
    ],

    __init__ : function(param1, param2) {

    },

    update: function() {
        this.super.update();

    }
 });

 >>> var sampleInstance = new SampleClass(null, null);

*/

var $ = function __class__() {
    // init parameters
    if (typeof (arguments[0]) == "string") {
        var className = arguments[0];
        var _prototype = arguments[1];
    } else {
        var className = null;
        var _prototype = arguments[0];
    }

    var scopeName = this.__name__;
    if (scopeName) {
        scopeName = scopeName.replace("[^A-Za-z0-9]", "");
    }
    if (!scopeName) {
        scopeName = null;
    }

    var superclass = _prototype.extends || BaseClass;
    var superclassPrototype = superclass.getClassPrototype();


    // prototype creation
    var prototype = {};//TODO исправить двойной вызов

    for (var name in superclassPrototype) {
        //////////////////////
        prototype[name] = superclassPrototype[name];
        /*if(typeof(superclassPrototype[name]) == "function"){
            alert("name "+name);
            prototype[name] = function(){
                superclassPrototype[name].apply(superclassPrototype, arguments);
            };
        }*/
        /////////////////////////
        //prototype[name] = superclassPrototype[name];
    }

    for (var name in _prototype) {
        if (name != "extends" && name != "includes") {
            prototype[name] = _prototype[name];
        }
    }

    for (var i in _prototype.includes) {
        var includedClass = _prototype.includes[i];
        var include = includedClass.getClassPrototype();
        for (var name in include) {
            var val = include[name];
            if (prototype[name] && typeof (val) == "function") {
                if (includedClass.isDirectField(name)) {
                    if (typeof (prototype[name]) == "function") {
                        if (prototype["_$" + name] && prototype["_$" + name].$parent == prototype) {
                            prototype["_$" + name].push(val);
                        } else {
                            prototype["_$" + name] = [prototype[name], val];
                            prototype["_$" + name].$parent = prototype;
                            prototype[name] = function (name) {
                                return function () {
                                    var result = null;
                                    var funcs = this["_$" + name];
                                    for (var i = 0; i < funcs.length; i++) {
                                        result = funcs[i].apply(this, arguments);
                                    }
                                    return result;
                                }
                            }(name);
                        }
                    } else {
                        throw new Error("failed to include function " + name + " into class " + className + ": type missmatch (function !=" + typeof (prototype[name]) + ")");
                    }
                }
            } else {
                prototype[name] = val;
            }
        }
    }

    prototype.$super = SuperClass(superclassPrototype);


    // class definition
    var clazz = function () {
        clazz.instantiateTo(this);

        this.__init__.apply(this, arguments);
        this.__load__();
    }

    clazz.instantiateTo = function (scope) {
        scope.$serialId = $.nextSerialId++;
        scope.super = prototype.$super(scope);
        scope.getClass = function () {
            return clazz;
        }

        Saver.registerObject(scope, clazz.$classSaverId);
        return instantiateTo(prototype, scope);
    }

    clazz.getClassPrototype = function () {
        return prototype;
    }

    clazz.getSuperClass = function () {
        return superclass;
    }

    clazz.isDirectInstance = function (instance) {
        return instance && instance.getClass && instance.getClass() == clazz;
    }

    clazz.isDirectField = function (name) {
        return _prototype.hasOwnProperty(name);
    }

    clazz.isInstance = function (instance) {
        if (instance && instance.getClass) {
            var clazz = instance.getClass();
            while (clazz) {
                if (clazz == clazz) {
                    return true;
                }

                clazz = clazz.getSuperClass();
            }
        }
        return false;
    }

    clazz.getName = function () {
        return className || "anonymous";
    }

    clazz.getScopeName = function () {
        return scopeName;
    }

    clazz.getFullName = function () {
        return className ? (scopeName + "$" + className) : "anonymous";
    }

    clazz.toString = function () {
        return "class " + this.getName();
    }

    // saver
    if (className) {
        if (scopeName) {
            clazz.$classSaverId = ClassSaver(clazz);
        } else {
            throw new Error("creating named class inside scope with no __name__");
        }
    } else {
        clazz.$classSaverId = AnonymousClassSaver;
    }

    return clazz;
};

$.nextSerialId = 0;

EXPORT("$", $);
EXPORT("__class__", $);