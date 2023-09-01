package com.sumit1334.copyproperties;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.util.YailList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CopyProperties extends AndroidNonvisibleComponent implements Component {
    private final Context context;
    private final String TAG = getClass().getSimpleName();
    private int height = -1;
    private int width = -1;

    public CopyProperties(ComponentContainer container) {
        super(container.$form());
        this.context = container.$context();
        Width(-1);
        Height(-1);
    }

    @SimpleProperty
    public void Height(int height) {
        this.height = height;
    }

    @SimpleProperty
    public void Width(int width) {
        this.width = width;
    }

    @SimpleFunction
    public void Copy(Component copyFrom, Component copyTo) throws InvocationTargetException, IllegalAccessException {
        String sN1 = copyFrom.getClass().getSimpleName();
        String sN2 = copyTo.getClass().getSimpleName();
        if (!sN1.equals(sN2)) {
            Toast.makeText(context, "copyFrom component is " + sN1 + " but copyTo component is a " + sN2, Toast.LENGTH_LONG).show();
            return;
        }
        Object value = null;
        String lastName = "";
        List<String> restricted = new ArrayList<>();
        restricted.add("HeightPercent");
        restricted.add("WidthPercent");
        if (!(copyFrom instanceof ComponentContainer)) {
            Log.i(TAG, "Copy: Given component is not a component container");
        }
        for (Method method : copyFrom.getClass().getMethods()) {
            if (method.getAnnotation(SimpleProperty.class) == null) {
                Log.i(TAG, "Copy: I am skipping " + method.getName() + " as it is not a property");
            } else {
                if (!method.getReturnType().getName().equals("void")) {
                    value = method.invoke(copyFrom);
                    lastName = method.getName();
                } else {
                    if (lastName.equals(method.getName())) {
                        if (!(restricted.contains(method.getName()))) {
                            if (method.getName().equals("Height"))
                                value = height;
                            if (method.getName().equals("Width"))
                                value = width;
                            Invoke(copyTo, method.getName(), YailList.makeList(new Object[]{value}));
                        }
                    }
                }
            }
        }
    }

    public Method getMethod(Method[] methods, String name, int parameterCount) {
        name = name.replaceAll("[^a-zA-Z0-9]", "");
        for (Method method : methods) {
            int methodParameterCount = method.getParameterTypes().length;
            if (method.getName().equals(name) && methodParameterCount == parameterCount) {
                return method;
            }
        }
        return null;
    }

    public void Invoke(Component component, String name, YailList parameters) {
        Method[] mMethods = component.getClass().getMethods();
        try {
            Object[] mParameters = parameters.toArray();
            ArrayList<Object> mParametersArrayList = new ArrayList<>();
            Method mMethod = getMethod(mMethods, name, mParameters.length);
            if (!(mMethod == null)) {
                Class<?>[] mRequestedMethodParameters = mMethod.getParameterTypes();
                for (int i = 0; i < mRequestedMethodParameters.length; i++) {
                    if ("int".equals(mRequestedMethodParameters[i].getName())) {
                        mParametersArrayList.add(Integer.parseInt(mParameters[i].toString()));
                    } else if ("float".equals(mRequestedMethodParameters[i].getName())) {
                        mParametersArrayList.add(Float.parseFloat(mParameters[i].toString()));
                    } else if ("double".equals(mRequestedMethodParameters[i].getName())) {
                        mParametersArrayList.add(Double.parseDouble(mParameters[i].toString()));
                    } else if ("java.lang.String".equals(mRequestedMethodParameters[i].getName())) {
                        mParametersArrayList.add(mParameters[i].toString());
                    } else if ("boolean".equals(mRequestedMethodParameters[i].getName())) {
                        mParametersArrayList.add(Boolean.parseBoolean(mParameters[i].toString()));
                    } else {
                        mParametersArrayList.add(mParameters[i]);
                    }
                }
            }

            if (mMethod == null)
                return;
            Log.i(TAG, "Invoke: method name : " + mMethod.getName());
            mMethod.invoke(component, mParametersArrayList.toArray());
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}