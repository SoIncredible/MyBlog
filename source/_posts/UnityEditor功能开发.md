---
title: UnityEditor功能开发
abbrlink: 2e6b555f
date: 2025-04-19 06:22:15
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

# 拓展UnityInspector窗口

```
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using UnityEditor;
using UnityEngine;

namespace DDZ
{
    [CustomEditor(typeof(RectTransform))]
    public class RectTransformEditor : Editor
    {
        Color defutColor;
        RectTransform rectTransform;
        UIRuntimeComponents runtimeComponents;
        Object[] components;
        Editor rectTransformEditor;
        static bool showCustom = true;

        GUIContent toggleLabel = new GUIContent(" 节点收集");
        GUIStyle style = null;
        
        private void OnEnable()
        {
            //反射获取ectTransformEditor
            var type = Assembly.GetAssembly(typeof(Editor)).GetType("UnityEditor.RectTransformEditor", false);
            if (type==null)
            {
                return;
            }
            rectTransformEditor = CreateEditor(target, type);

            rectTransform = (RectTransform) target;
            if (rectTransform != null)
            {
                defutColor = GUI.backgroundColor;
                
                //获取RuntimeComponents组件
                runtimeComponents = FindUIRuntimeComponents(rectTransform.parent);
            
                //收集节点，添加GameObject，并且删除CanvasRenderer
                components = rectTransform.GetComponents<Component>();
                var list = components.ToList();
                list.Add(rectTransform.gameObject);
                int len = list.Count;
                for (int i = len - 1; i >= 0; i--)
                {
                    var component = list[i];
                    var typeStr = component.GetType().Name;
                    if (typeStr == "CanvasRenderer")
                    {
                        list.RemoveAt(i);
                        break;
                    }
                }
                components = list.ToArray();
            }
        }

        public override void OnInspectorGUI()
        {
            if (rectTransform == null)
            {
                return;
            }

            if (rectTransformEditor)
            {
                rectTransformEditor.OnInspectorGUI();
            }
            
            if (runtimeComponents == null)
            {
                return;
            }
            
            EditorGUILayout.Space();
            Rect xian1 = GUILayoutUtility.GetRect(GUIContent.none, GUIStyle.none, GUILayout.Height(1));
            Rect backgroundRect = GUILayoutUtility.GetRect(GUIContent.none, GUIStyle.none, GUILayout.Height(20));

            EditorGUI.DrawRect(xian1, Color.black);
            EditorGUI.DrawRect(backgroundRect, new Color(0.25f, 0.25f, 0.25f));

            // 绘制内容区域
            if (style==null)
            {
                style = new GUIStyle(EditorStyles.foldout);
                style.fontStyle = FontStyle.Bold;
            }
            Rect contentRect = new Rect(backgroundRect.x-15 , backgroundRect.y, backgroundRect.width+20, backgroundRect.height);
            showCustom = GUI.Toggle(contentRect, showCustom, toggleLabel,style);

            if (showCustom)
            {
                int len = components.Length;
                for (int i = 0; i < len; i++)
                {
                    var component = components[i];
                    var typeStr = component.GetType().Name;
                    if (IsCollected(runtimeComponents, component))
                    {
                        GUI.backgroundColor = Color.green;
                    }
                    else
                    {
                        GUI.backgroundColor = defutColor;
                    }

                    if (i % 3 == 0)
                    {
                        GUILayout.BeginHorizontal();
                    }

                    if (i == len - 1)
                    {
                        if (GUILayout.Button(typeStr, GUILayout.Width(100), GUILayout.Height(30)))
                        {
                            OperateComponent(component);
                        }
                    }
                    else
                    {
                        if (GUILayout.Button(typeStr, GUILayout.Height(30)))
                        {
                            OperateComponent(component);
                        }
                    }

                    if (i % 3 == 2 || i == len - 1)
                    {
                        GUILayout.EndHorizontal();
                    }
                }

                EditorGUI.indentLevel--;
            }
        }

        private void OnDisable()
        {
            if (rectTransformEditor)
            {
                DestroyImmediate(rectTransformEditor);
                rectTransform = null;
                runtimeComponents = null;
                components = null;
            }
        }

        void OperateComponent(Object component)
        {
            if (Event.current.button == 0)
            {
                if (!Application.isPlaying)
                {
                    if (IsCollected(runtimeComponents, component))
                    {
                        DeleteObject(runtimeComponents, component);
                    }
                    else
                    {
                        AddObject(runtimeComponents, component);
                    }
                            
                    EditorApplication.ExecuteMenuItem("Window/General/Hierarchy");
                }
            }
            else if (Event.current.button == 1)
            {
                if (IsCollected(runtimeComponents, component))
                {
                    FindReferences(runtimeComponents, component);
                }
            }
        }

        bool IsCollected(UIRuntimeComponents runtimeComponents, Object component)
        {
            if (runtimeComponents.m_objects==null)
            {
                return false;
            }
            int len = runtimeComponents.m_objects.Length;
            for (int i = 0; i < len; i++)
            {
                if (runtimeComponents.m_objects[i] == component)
                {
                    return true;
                }
            }

            return false;
        }

        void AddObject(UIRuntimeComponents runtimeComponents, Object component)
        {
            if (runtimeComponents.m_objects==null)
            {
                runtimeComponents.m_objects = new Object[0];
            }
            int len = runtimeComponents.m_objects.Length;
            for (int i = 0; i < len; i++)
            {
                if (runtimeComponents.m_objects[i] == component)
                {
                    return;
                }
            }
            
            var list = runtimeComponents.m_objects.ToList();
            list.Add(component);
            runtimeComponents.m_objects = list.ToArray();
            SavePrefab(runtimeComponents);
        }

        void DeleteObject(UIRuntimeComponents runtimeComponents, Object component)
        {
            var list = runtimeComponents.m_objects.ToList();
            int len = list.Count;
            for (int i = 0; i < len; i++)
            {
                if (list[i] == component)
                {
                    list.RemoveAt(i);
                    break;
                }
            }
            runtimeComponents.m_objects = list.ToArray();
            if (runtimeComponents.m_objects.Length<=0)
            {
                runtimeComponents.m_objects = null;
            }
            SavePrefab(runtimeComponents);
        }

        void FindReferences(UIRuntimeComponents runtimeComponents, Object component)
        {
            EditorGUIUtility.PingObject(runtimeComponents.gameObject);
            Selection.objects = new Object[] {runtimeComponents.gameObject};

            var len = runtimeComponents.m_objects.Length;
            for (int i = 0; i < len; i++)
            {
                if (component == runtimeComponents.m_objects[i])
                {
                    runtimeComponents.focusIndex = i;
                }
            }
        }

        UIRuntimeComponents FindUIRuntimeComponents(Transform transform)
        {
            if (transform == null)
                return null;

            var runtimeComponents = transform.GetComponent<UIRuntimeComponents>();
            if (runtimeComponents != null)
                return runtimeComponents;

            return FindUIRuntimeComponents(transform.parent);
        }

        void SavePrefab(UIRuntimeComponents runtime)
        {
            if (runtime == null)
            {
                return;
            }

            GameObject save_go = runtime.gameObject;
            var assetType = PrefabUtility.GetPrefabAssetType(runtime.gameObject);
            if (assetType == PrefabAssetType.Regular) //如果是预制体的一部分
            {
                save_go = PrefabUtility.GetOutermostPrefabInstanceRoot(runtime.gameObject);
            }

            if (EditorUtility.IsPersistent(save_go))
            {
                EditorUtility.SetDirty(save_go);
            }
            else
            {
                string path =
                    AssetDatabase.GetAssetPath(PrefabUtility.GetCorrespondingObjectFromOriginalSource(save_go));
                if (string.IsNullOrEmpty(path))
                {
                    EditorUtility.SetDirty(save_go);
                }
                else
                {
                    PrefabUtility.SaveAsPrefabAssetAndConnect(save_go, path, InteractionMode.UserAction);
                }
            }
        }
    }
}

```

# UnityEditor下的加载弹窗或者弹窗
```
private static void ShowProgress(float progress, int total, int current)
{
    EditorUtility.DisplayProgressBar("Searching",
        string.Format("Checking ({0}/{1}), please wait...", current, total), progress);
}
```

```
EditorUtility.DisplayDialog( "", "Board64中不存在对Board100的依赖", "OK" );
```

# 资源导入管线处理

# 参考资料
- https://blog.csdn.net/yx314636922/article/details/126872839
- https://blog.csdn.net/angry_youth/article/details/117469722