---
title: Unity实现Text部分区域可响应点击
tags: 
  - UGUI
abbrlink: 1de61da1
categories: 硬技能
date: 2024-11-29 22:53:16
cover: https://www.notion.so/images/page-cover/met_fitz_henry_lane.jpg
description: 
---

源码

```
using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.Text.RegularExpressions;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;

namespace UI.UIItems
{
    public class HyperlinkText : Text, IPointerClickHandler
    {
        protected override void Awake()
        {
            Set_TextLinkFuncCB(Application.OpenURL);
        }
        /// <summary>
        /// 解析完最终的文本
        /// </summary>
        private string m_OutputText;

        /// <summary>
        /// 超链接信息列表
        /// </summary>
        private readonly List<HrefInfo_> m_HrefInfos = new List<HrefInfo_>();

        /// <summary>
        /// 文本构造器
        /// </summary>
        protected static readonly StringBuilder s_TextBuilder = new StringBuilder();

        /// <summary>
        /// 超链接正则
        /// </summary>
        private static readonly Regex s_HrefRegex =
            new Regex(@"<a href=([^>\n\s]+)>(.*?)(</a>)", RegexOptions.Singleline);

        //
        private static readonly Regex s_VertexFilter = new Regex(@"(|[ \n\r\t]+)", RegexOptions.Singleline);

        VertexHelper _toFill = null;
        /// <summary>
        /// 是否使用超链接  默认未False
        /// </summary>
        bool bool_IsLink = true;

        private Action<string> linkFunc_Cb = null;

        private RectTransform rect_Parent;
        private RectTransform Rect_Parent
        {
            get
            {
                if (rect_Parent == null)
                {
                    Transform trans = this.transform.parent != null ? this.transform.parent.transform : this.transform;
                    rect_Parent = trans.GetComponent<RectTransform>();
                }

                return rect_Parent;
            }
        }

        //设置 文本 超链接的点击回调事件
        public void Set_TextLinkFuncCB(Action<string> linkFunc_Cb)
        {
            bool_IsLink = true;
            if (this.linkFunc_Cb != null)
            {
                this.linkFunc_Cb = null;
            }
            this.linkFunc_Cb = linkFunc_Cb;
            OnPopulateMesh(_toFill);
        }

        //字符顶点数
        const int perCharVerCount = 4;

        /// <summary>
        /// 文本构造器
        /// </summary>
        protected static readonly StringBuilder textRebuild = new StringBuilder();

        protected override void OnPopulateMesh(VertexHelper toFill)
        {
            if (toFill == null)
            {
                return;
            }
            _toFill = toFill;
            //TODO 编辑器状态下这里不执行， 方便调试看到效果用
            if (!bool_IsLink)
            {
                m_Text = GetOutputText_Nomal(text);
                base.OnPopulateMesh(toFill);
                return;
            }

            var orignText = m_Text;
            m_OutputText = GetOutputText_Init(text);
            m_Text = m_OutputText;
            text = m_OutputText;
            base.OnPopulateMesh(toFill);
            m_Text = orignText;
            GetOutputText(text, toFill.currentVertCount);

            UIVertex vert = new UIVertex();

            // 处理超链接包围框
            foreach (var hrefInfo in m_HrefInfos)
            {
                hrefInfo.boxes.Clear();
                if (hrefInfo.startIndex >= toFill.currentVertCount)
                {
                    continue;
                }

                // 将超链接里面的文本顶点索引坐标加入到包围框
                toFill.PopulateUIVertex(ref vert, hrefInfo.startIndex);
                var pos = vert.position;
                var bounds = new Bounds(pos, Vector3.zero);
                Vector3 previousPos = Vector3.zero;
                for (int i = hrefInfo.startIndex, m = hrefInfo.endIndex; i < m; i++)
                {
                    if (i >= toFill.currentVertCount)
                    {
                        break;
                    }

                    toFill.PopulateUIVertex(ref vert, i);
                    pos = vert.position;
                    if ((i - hrefInfo.startIndex) % 4 == 1)
                    {
                        previousPos = pos;
                    }
                    if (previousPos != Vector3.zero && (i - hrefInfo.startIndex) % 4 == 0 && pos.x < previousPos.x) // 换行重新添加包围框
                    {
                        hrefInfo.boxes.Add(new Rect(bounds.min, bounds.size));
                        bounds = new Bounds(pos, Vector3.zero);
                    }
                    else
                    {
                        bounds.Encapsulate(pos); // 扩展包围框
                    }
                }
                hrefInfo.boxes.Add(new Rect(bounds.min, bounds.size));
            }


            if (this.gameObject.activeInHierarchy)
            {
                StartCoroutine(RefrehLayout());
            }
        }

        IEnumerator RefrehLayout()
        {
            yield return new WaitForEndOfFrame();
            LayoutRebuilder.ForceRebuildLayoutImmediate(Rect_Parent);
        }

        //初始化超链接文本 获取最终结果的定点数用
        string GetOutputText_Init(string outputText)
        {
            s_TextBuilder.Length = 0;
            m_HrefInfos.Clear();
            var indexText = 0;
            foreach (Match match in s_HrefRegex.Matches(outputText))
            {
                s_TextBuilder.Append(outputText.Substring(indexText, match.Index - indexText));
                s_TextBuilder.Append(match.Groups[2].Value);
                indexText = match.Index + match.Length;
            }
            s_TextBuilder.Append(outputText.Substring(indexText, outputText.Length - indexText));
            return s_TextBuilder.ToString();
        }

        /// <summary>
        /// 获取超链接解析后的最后输出文本
        /// </summary>
        /// <returns></returns>
        string GetOutputText(string outputText, int currentVertCount)
        {
            s_TextBuilder.Length = 0;
            m_HrefInfos.Clear();
            var indexText = 0;
            int vertCount = Regex.Replace(Regex.Replace(outputText.ToString(), @"\s", ""), @"<(.*?)>", "").Length * 4;
            int vercCount_Offset_Start = 0;
            int vercCount_Offset_End = 0;
            bool isLineCup = false;
            if (currentVertCount > vertCount)
            {
                isLineCup = true;
                vercCount_Offset_Start = 80;
                vercCount_Offset_End = 88;
            }
            foreach (Match match in s_HrefRegex.Matches(outputText))
            {
                s_TextBuilder.Append(outputText.Substring(indexText, match.Index - indexText));
                int offset_Len = 0;
                if (isLineCup)
                {
                    offset_Len = (s_TextBuilder.Length - Regex.Replace(s_TextBuilder.ToString(), @"<(.*?)>", "").Length) * 4;
                }

                s_TextBuilder.Append("<i><b><color=#f49037>");  // 超链接颜色

                var str = Regex.Replace(s_TextBuilder.ToString(), @"\s", "");
                var group = match.Groups[1];
                var hrefInfo = new HrefInfo_
                {
                    startIndex = Regex.Replace(str, @"<(.*?)>", "").Length * 4 + vercCount_Offset_Start + offset_Len, // 超链接里的文本起始顶点索引
                    endIndex = (Regex.Replace(str, @"<(.*?)>", "").Length +
                        Regex.Replace(Regex.Replace(match.Groups[2].ToString(), @"\s", "")
                            , @"<(.*?)>", "").Length - 1) * 4 + 3 + vercCount_Offset_End + offset_Len,
                    name = group.Value
                };
                m_HrefInfos.Add(hrefInfo);
                //Debug.Log($"顶点信息，开始的：{hrefInfo.startIndex}，结束的：{hrefInfo.endIndex}");

                s_TextBuilder.Append(match.Groups[2].Value);
                s_TextBuilder.Append("</color></b></i>");
                indexText = match.Index + match.Length;
            }

            s_TextBuilder.Append(outputText.Substring(indexText, outputText.Length - indexText));
            return s_TextBuilder.ToString();
        }

        //获取祛除掉超链接 保留普通文本  保证配置里超链接标签的文本  在其他地方也可以正常使用，只有调用了超链接初始化的  才会给超链接形式的文本
        string GetOutputText_Nomal(string outputText)
        {
            s_TextBuilder.Length = 0;
            m_HrefInfos.Clear();
            var indexText = 0;
            MatchCollection matchs = s_HrefRegex.Matches(outputText);
            if (matchs.Count <= 0)
            {
                return outputText;
            }
            foreach (Match match in matchs)
            {
                s_TextBuilder.Append(outputText.Substring(indexText, match.Index - indexText));
                s_TextBuilder.Append(match.Groups[2].Value);
                indexText = match.Index + match.Length;
            }
            s_TextBuilder.Append(outputText.Substring(indexText, outputText.Length - indexText));
            return s_TextBuilder.ToString();
        }

        public void OnPointerClick(PointerEventData eventData)
        {
            Vector2 lp;
            RectTransformUtility.ScreenPointToLocalPointInRectangle(
                rectTransform, eventData.position, eventData.pressEventCamera, out lp);

            foreach (var hrefInfo in m_HrefInfos)
            {
                var boxes = hrefInfo.boxes;
                for (var i = 0; i < boxes.Count; ++i)
                {
                    if (boxes[i].Contains(lp))
                    {
                        //Debug.Log("技能 超链接 点击了：" + hrefInfo.name);
                        linkFunc_Cb?.Invoke(hrefInfo.name);
                        return;
                    }
                }
            }
        }
    }
    /// <summary>
    /// 超链接信息类
    /// </summary>
    class HrefInfo_
    {
        public int startIndex;

        public int endIndex;

        public string name;

        public readonly List<Rect> boxes = new List<Rect>();
    }       
}
```

使用
```
    var terms2 = $"<color=#aa3900><a herf={PrivacyPolicyDefine.UrlService}>{ScriptLocalization.terms_of_use}</a></color>";

    var policy2 = $"<color=#aa3900><a herf={PrivacyPolicyDefine.UrlPrivacy}>{ScriptLocalization.privacy_policy}</a></color>";

    _ui.contentText.text = string.Format(ScriptLocalization.terms_policy_dlg_content, terms2, policy2);
```