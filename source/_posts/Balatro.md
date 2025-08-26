---
title: Balatro
cover: 'https://www.notion.so/images/page-cover/woodcuts_15.jpg'
abbrlink: 254a2567
date: 2025-01-23 22:15:30
tags:
categories:
description:
swiper_index:
sticky:
---

小丑牌使用love2d游戏引擎开发,开发语言是lua.笔者之前从未接触过lua,在上手阅读前还是有一些开发语言上的语法障碍需要克服一下.看起来小丑牌底层使用到了一个名叫`SNKRX`游戏的底层框架.

# 环境安装

- [love2d游戏引擎地址](https://love2d.org/wiki/Main_Page)
- [lua地址](https://www.lua.org/)

# 运行游戏

首先在`~/.zshrc`中添加以下文本
```shell
# alias to love
alias love="/Applications/love.app/Contents/MacOS/love"
```

退出`zshrc`,执行
```shell
source ~/.zshrc
```

来到小丑牌的根目录,执行:
```shell
love .
```

# 程序入口

拿到小丑牌源码,程序的入口在`main.lua`中,首先看到的是一串`require`语句,该语句后面跟的内容就是脚本的路径,且会执行该路径下的脚本.就拿`game.lua`脚本中的`Game`类举例,首先是`main.lua`的require语句中请求模块`globals`,lua找到并执行`globals.lua`脚本,在该脚本中创建了一个`Game`对象,在`Game`对象的构造方法中创建了一个Game的单例.执行完`require "globals"`之后,`Game`类的单例就已经被创建出来了,并可以被全局调用.

# 更新逻辑

游戏的update逻辑在`game.lua`脚本的`function Game:update(dt)`中.
在`globals.lua`中,可以看到游戏状态机中的状态:
```lua
self.STATES = {
        SELECTING_HAND = 1,
        HAND_PLAYED = 2,
        DRAW_TO_HAND = 3,
        GAME_OVER = 4,
        SHOP = 5,
        PLAY_TAROT = 6,
        BLIND_SELECT = 7,
        ROUND_EVAL = 8,
        TAROT_PACK = 9,
        PLANET_PACK = 10,
        MENU = 11,
        TUTORIAL = 12,
        SPLASH = 13,--DO NOT CHANGE, this has a dependency in the SOUND_MANAGER
        SANDBOX = 14,
        SPECTRAL_PACK = 15,
        DEMO_CTA = 16,
        STANDARD_PACK = 17,
        BUFFOON_PACK = 18,
        NEW_ROUND = 19,
    }
```

# 配置

小丑牌的配置在`functions/misc_functions.lua`脚本中,在这里你可以修改游戏的初始参数,比如初始金币、手牌上限、小丑牌栏位上限等等.

```lua
function get_starting_params()
return {
    dollars = 100,
    hand_size = 15,
    discards = 3,
    hands = 4,
    reroll_cost = 5,
    joker_slots = 7,
    ante_scaling = 1,
    consumable_slots = 2,
    no_faces = false,
    erratic_suits_and_ranks = false,
  }
end
```

# 交互


主界面的绘制在`UI_definitions.lua`脚本中,在小丑牌中页面的拼接用的是类似json一样的嵌套结构来描述UI的信息,重点关注`UIBox_button`的button参数,该参数指明该按钮和其相应点击的事件.
```lua
function create_UIBox_main_menu_buttons()
  local text_scale = 0.45
  local language = nil
  if not G.F_ENGLISH_ONLY then 
    language = Sprite(0,0,0.6,0.6,G.ASSET_ATLAS["icons"], {x=2, y=0})
    language.states.drag.can = false
  end
  local discord = nil
  local twitter = nil
  if G.F_DISCORD then 
    discord = Sprite(0,0,0.6,0.6,G.ASSET_ATLAS["icons"], {x=0, y=0})
    discord.states.drag.can = false
    twitter = Sprite(0,0,0.6,0.6,G.ASSET_ATLAS["icons"], {x=0, y=1})
    twitter.states.drag.can = false
  end

  local quit_func = 'quit'

  local t = {
    n=G.UIT.ROOT, config = {align = "cm",colour = G.C.CLEAR}, nodes={ 
      {n=G.UIT.C, config={align = "bm"}, nodes={      
                    {
                        n = G.UIT.R,
                        config = { align = "cm", padding = 0.2, r = 0.1, emboss = 0.1, colour = G.C.L_BLACK, mid = true },
                        nodes = {
          -- 如果没有完成教程,返回start_run 如果完成教程返回setup_run
          UIBox_button{id = 'main_menu_play', button = not G.SETTINGS.tutorial_complete and "start_run" or "setup_run", colour = G.C.BLUE, minw = 3.65, minh = 1.55, label = {localize('b_play_cap')}, scale = text_scale*2, col = true},
          {n=G.UIT.C, config={align = "cm"}, nodes={
            UIBox_button{button = 'options', colour = G.C.ORANGE, minw = 2.65, minh = 1.35, label = {localize('b_options_cap')}, scale = text_scale * 1.2, col = true},
            G.F_QUIT_BUTTON and {n=G.UIT.C, config={align = "cm", minw = 0.2}, nodes={}} or nil,
            G.F_QUIT_BUTTON and UIBox_button{button = quit_func, colour = G.C.RED, minw = 2.65, minh = 1.35, label = {localize('b_quit_cap')}, scale = text_scale * 1.2, col = true} or nil,
          }},
          UIBox_button{id = 'collection_button', button = "your_collection", colour = G.C.PALE_GREEN, minw = 3.65, minh = 1.55, label = {localize('b_collection_cap')}, scale = text_scale*1.5, col = true},
        }},
      }},
      {n=G.UIT.C, config={align = "br", minw = 3.2, padding = 0.1}, nodes={
        G.F_DISCORD and {n=G.UIT.R, config = {align = "cm", padding = 0.2}, nodes={
          {n=G.UIT.C, config={align = "cm", padding = 0.1, r = 0.1, hover = true, colour = mix_colours(G.C.BLUE, G.C.GREY, 0.4), button = 'go_to_discord', shadow = true}, nodes={
            {n=G.UIT.O, config={object = discord}},
          }},
          {n=G.UIT.C, config={align = "cm", padding = 0.1, r = 0.1, hover = true, colour = G.C.BLACK, button = 'go_to_twitter', shadow = true}, nodes={
            {n=G.UIT.O, config={object = twitter}},
          }}
        }} or nil,
        not G.F_ENGLISH_ONLY and {n=G.UIT.R, config = {align = "cm", padding = 0.2, r = 0.1, emboss = 0.1, colour = G.C.L_BLACK}, nodes={
          {n=G.UIT.R, config={align = "cm", padding = 0.15, minw = 1, r = 0.1, hover = true, colour = mix_colours(G.C.WHITE, G.C.GREY, 0.2), button = 'language_selection', shadow = true}, nodes={
            {n=G.UIT.O, config={object = language}},
            {n=G.UIT.T, config={text = G.LANG.label, scale = 0.4, colour = G.C.UI.TEXT_LIGHT, shadow = true}}
          }}
        }} or nil,
      }},
    }}
  return t
end
```
拿主界面上的quit按钮举例,`quit_func`就是指向了一个内容为`quit`的字符串,不过其实际上指向的是`main.lua`脚本中的`love.quit()`方法,目前暂不清楚究竟是如何关联起来的.

真正进入游戏的是通过`start_setup_run`方法,然后会进入`BlindSelect(盲注)阶段`->`New Round`->`DrawToHand`


# 洗牌算法

在`state_events.lua`脚本中`new round`方法内`G.deck:shuffle('nr'..G.GAME.round_resets.ante)`这一行.
```lua
G.E_MANAGER:add_event(Event({
  trigger = 'immediate',
  func = function()
      print("进入DrawToHand阶段")
      G.STATE = G.STATES.DRAW_TO_HAND
      -- 在此处执行洗牌操作
      G.deck:shuffle('nr'..G.GAME.round_resets.ante)
      G.deck:hard_set_T()
      G.STATE_COMPLETE = false
      return true
  end
}))
```
洗牌算法的具体实现在`cardarea.lua`中.
```lua
function CardArea:shuffle(_seed)
    pseudoshuffle(self.cards, pseudoseed(_seed or 'shuffle'))
    self:set_ranks()
end

function pseudoshuffle(list, seed)
  if seed then math.randomseed(seed) end

  if list[1] and list[1].sort_id then
    table.sort(list, function (a, b) return (a.sort_id or 1) < (b.sort_id or 2) end)
  end

  for i = #list, 2, -1 do
		local j = math.random(i)
		list[i], list[j] = list[j], list[i]
	end
end

```
该算法名为`Fisher–Yates shuffle 洗牌算法`,算法的基本思想如下:

1. 给定一个含有n个数的序列,标记index为1-n
2. 从1-n中随机一个数k
3. 找到index为k的数字,将其与该序列的最后一位进行交换.接下来会从(1 - n-1)范围内寻找新的随机数k,与序列的倒数第二位进行交换.
4. 重复步骤2、3直到所有的数都被取完.

# 机制




# 参考资料
- https://gaohaoyang.github.io/2016/10/16/shuffle-algorithm/
