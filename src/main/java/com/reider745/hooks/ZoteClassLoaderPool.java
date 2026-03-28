package com.reider745.hooks;

import java.util.ArrayList;

import org.mozilla.javascript.Context;

public class ZoteClassLoaderPool extends ClassLoader {
	private static final ArrayList<ClassLoader> loaders = new ArrayList<>();

	public ZoteClassLoaderPool(ClassLoader parent) {
		super(parent);
	}

	public static void addClassLoader(ClassLoader loader) {
		loaders.add(loader);
	}

	public static Class<?> forName(String name) throws ClassNotFoundException {
		try {
			return Context.getCurrentContext().getApplicationClassLoader().loadClass(name);
		} catch (Exception ignore) {}
		return Class.forName(name);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		for (ClassLoader loader : loaders) {
			try {
				return loader.loadClass(name);
			} catch (ClassNotFoundException ignore) {}
		}
		return super.findClass(name);
	}
}
