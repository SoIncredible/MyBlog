---
title: UnityEditor功能开发
abbrlink: 2e6b555f
date: 2025-04-19 06:22:15
tags:
categories: UnityEditor开发
cover: https://public.ysjf.com/mediastorm/material/material/%E8%87%AA%E7%84%B6%E9%A3%8E%E5%85%89_%E6%9C%9F%E6%9C%AB%E7%9A%84%E5%BB%B6%E6%97%B6_02_%E5%85%A8%E6%99%AF.jpg
description:
swiper_index:
sticky:
---

# GUI、EditorGUI、GUILayout、EditorGUILayout、EditorGUIUtility、GUIUtility

# 在Editor下使用异步

## 多线程方式

插件Figma Convert Unity中就使用了多线程的方式

## 使用协程

Unity提供的一些运行时I/O向的操作, 可以通过使其支持await的方式在Editor模式下使用



# 拓展UnityInspector窗口

```C#
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
```C#
private static void ShowProgress(float progress, int total, int current)
{
    EditorUtility.DisplayProgressBar("Searching",
        string.Format("Checking ({0}/{1}), please wait...", current, total), progress);
}
```

```C#
EditorUtility.DisplayDialog( "", "Board64中不存在对Board100的依赖", "OK" );
```

# 资源导入管线处理

# Unity 内建图标列表

通过`EditorGUIUtility.FindTexture`接口可以拿到UnityEditor中内置的图标

https://www.cnblogs.com/CloudLiu/p/9957335.html

# 在Scene窗口中添加按钮
using UnityEngine;
using UnityEditor;

[CustomEditor(typeof(Particle2DUGUI))]
public class Particle2DUGUIEditor : Editor {

	public override void OnInspectorGUI(){
		Particle2DUGUI particle2D = target as Particle2DUGUI;
		EditorGUILayout.Space();

		Color c = GUI.backgroundColor;
		if(particle2D.material==null){
			GUI.backgroundColor =  Color.red;
			EditorGUILayout.TextArea("Error: Material is NULL");
			GUI.backgroundColor =  c;
		} else if(particle2D.mainTexture==null){
			GUI.backgroundColor =  Color.red;
			EditorGUILayout.TextArea("Error: Texture is NULL");
			GUI.backgroundColor =  c;
		}

		serializedObject.Update();
		EditorGUILayout.PropertyField(serializedObject.FindProperty("m_Material"), true);
		EditorGUILayout.PropertyField(serializedObject.FindProperty("m_Color"), true);
		EditorGUILayout.PropertyField(serializedObject.FindProperty("speedScale"), true);
		EditorGUILayout.PropertyField(serializedObject.FindProperty("playOnAwake"), true);
		if(particle2D.playOnAwake){
			EditorGUILayout.PropertyField(serializedObject.FindProperty("delayPlay"), true);
		}
		if(!particle2D.configValues.isLooop){
			EditorGUILayout.PropertyField(serializedObject.FindProperty("autoRemove"), true);
		}else{
			EditorGUILayout.PropertyField(serializedObject.FindProperty("prewarm"), true);
		}
		EditorGUILayout.PropertyField(serializedObject.FindProperty("simulationSpace"), true);
		EditorGUILayout.PropertyField(serializedObject.FindProperty("rectTransAutosize"), true);
		EditorGUILayout.Space();
		EditorGUILayout.PropertyField(serializedObject.FindProperty("effectConfig"), true);

		c = GUI.backgroundColor;
		GUI.backgroundColor =  Color.yellow;
		EditorGUILayout.TextArea("Support: Pex Or Plist config File.");

		GUI.backgroundColor =  Color.green;
		if(particle2D.effectConfig!=null){
			EditorGUILayout.BeginHorizontal();
			if(GUILayout.Button("Load From Config",GUILayout.Height(24))){
				particle2D.ReadConfig();
				particle2D.ResetParticle();
			}
			EditorGUILayout.EndHorizontal();
		}else{
			GUI.enabled = false;
			GUILayout.Button("Load From Config",GUILayout.Height(24));
			GUI.enabled = true;
		}
		GUI.backgroundColor = c;

		EditorGUILayout.PropertyField(serializedObject.FindProperty("configValues"),true);
		serializedObject.ApplyModifiedProperties();

		c = GUI.backgroundColor;
		GUI.backgroundColor =  Color.green;
		EditorGUILayout.BeginHorizontal();
		if(GUILayout.Button("Refresh",GUILayout.Height(32))){
			particle2D.ResetParticle();
		}
		EditorGUILayout.EndHorizontal();
		GUI.backgroundColor = c;

		if(!Application.isPlaying && Selection.activeObject==particle2D.gameObject){
			if(particle2D.configValues!=null && particle2D.Emitter!=null && particle2D.configValues.maxParticles!=particle2D.Emitter.capacity){
				particle2D.ResetParticle();
			}
			EditorUtility.SetDirty (particle2D);
			HandleUtility.Repaint();
		}
	}

	void OnSceneGUI(){
		Particle2DUGUI particle2D = target as Particle2DUGUI;

		Handles.BeginGUI();
		GUI.backgroundColor =  Color.green;
		GUILayout.BeginArea(new Rect(5,5,60,130));
		if (GUILayout.Button("Play",GUILayout.Width(60),GUILayout.Height(30)))
		{
			if(particle2D.Emitter!=null) {
				particle2D.Emitter.Play();
			}else{
				particle2D.ResetParticle();
			}
			particle2D.OnRebuildRequested();
		}
		if (GUILayout.Button("Stop",GUILayout.Width(60),GUILayout.Height(30)))
		{
			if(particle2D.Emitter!=null) {
				if(particle2D.Emitter!=null) {
					particle2D.Emitter.Stop(false);
				}
			}
		}
		if (GUILayout.Button("Clear",GUILayout.Width(60),GUILayout.Height(30)))
		{
			if(particle2D.Emitter!=null) {
				if(particle2D.Emitter!=null) {
					particle2D.Emitter.Stop(true);
					particle2D.OnRebuildRequested();
				}
			}
		}
		GUILayout.EndArea();
		Handles.EndGUI();
	}

	void OnEnable(){
		if(!Application.isPlaying){
			Particle2DUGUI particle2D = target as Particle2DUGUI;
			if(particle2D!=null && Selection.activeGameObject == particle2D.gameObject && particle2D.playOnAwake) {
				if(particle2D.Emitter!=null) {
					particle2D.Emitter.Play();
				}else{
					if(particle2D.configValues==null) particle2D.configValues = new Particle2DConfig();
					if(particle2D.configValues!=null && particle2D.configValues.texture==null){
						//show default texture
						Object[] unityAssets = AssetDatabase.LoadAllAssetsAtPath("Resources/unity_builtin_extra");
						foreach(Object obj in unityAssets){
							if(obj.name.Equals("Default-Particle")){
								particle2D.configValues.texture = obj as Texture;
								break;
							}
						}
					}
					particle2D.ResetParticle();
				}
			}
		}
	}

	void OnDisable(){
		if(!Application.isPlaying){
			Particle2DUGUI particle2D = target as Particle2DUGUI;
			if(particle2D!=null && Selection.activeGameObject != particle2D.gameObject && particle2D.Emitter!=null) {
				if(particle2D.Emitter!=null) {
					particle2D.Emitter.Stop(true);
					particle2D.OnRebuildRequested();
				}
			}
		}
	}

	[MenuItem("Particle2D/Particle2D UGUI")]
	static void CreateParticle2DSystem(){
		GameObject go = new GameObject("Particle2D UGUI");
		Particle2DUGUI ugui = go.AddComponent<Particle2DUGUI>();
		GameObject canvas = GameObject.Find("Canvas");
		if(canvas){
			go.transform.SetParent(canvas.transform);
			go.transform.localScale = Vector3.one;
			go.transform.localPosition = Vector3.zero;
		}
		MonoScript ms = MonoScript.FromMonoBehaviour(ugui);
		string filePath = AssetDatabase.GetAssetPath( ms );
		filePath = filePath.Substring(0,filePath.LastIndexOf("Scripts/Particle2DUGUI.cs"));
		filePath += "Materials/UGUI_Additive.mat";
		Material mat = AssetDatabase.LoadAssetAtPath<Material>(filePath);
		ugui.material = mat;
	}
}


# 在Play按钮旁边添加按钮

TEngine



# 参考资料
- https://blog.csdn.net/yx314636922/article/details/126872839
- https://blog.csdn.net/angry_youth/article/details/117469722