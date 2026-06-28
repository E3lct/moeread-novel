<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.moeread.model.*" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) request.getAttribute("user");
    @SuppressWarnings("unchecked")
    List<Book> importHistory = (List<Book>) request.getAttribute("importHistory");
    String ctx = request.getContextPath();
    String saved = request.getParameter("saved");
    String err = request.getParameter("err");

    boolean hasAvatar = user.getAvatar() != null && !user.getAvatar().isEmpty();
    boolean hasMascot = user.getMascotImage() != null && !user.getMascotImage().isEmpty();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>墨读 - 设置</title>
    <link rel="stylesheet" href="<%=ctx%>/assets/css/variables.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/layout.css">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/settings.css">
</head>
<body>
<%@ include file="navbar.jsp" %>

<div class="settings-page">
<div class="settings-content">

    <div class="page-header">
        <h1 class="page-title">设置</h1>
    </div>

    <% if ("1".equals(saved)) { %><div class="alert alert-success">保存成功</div><% } %>
    <% if ("pwd".equals(err)) { %><div class="alert alert-error">旧密码不正确</div><% } %>
    <% if ("short".equals(err)) { %><div class="alert alert-error">新密码至少 6 位</div><% } %>

    <!-- ===== 信息修改 ===== -->
    <div class="accordion-item">
        <div class="accordion-head" onclick="toggleAccordion(this)">
            <span class="accordion-title">信息修改</span>
            <span class="accordion-arrow"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6,9 12,15 18,9"/></svg></span>
        </div>
        <div class="accordion-body">
            <form action="<%=ctx%>/settings" method="post" enctype="multipart/form-data" class="settings-form">
                <input type="hidden" name="action" value="update_profile">
                <input type="hidden" name="mascotOpacity" value="<%=user.getMascotOpacity() > 0 ? user.getMascotOpacity() : 80%>">
                <div class="form-row">
                    <label class="form-label">账号</label>
                    <div class="form-static"><%=user.getUsername()%></div>
                </div>
                <div class="form-row">
                    <label class="form-label" for="nickname">昵称</label>
                    <input type="text" id="nickname" name="nickname" class="form-input"
                           value="<%=user.getNickname() != null ? user.getNickname() : ""%>" maxlength="20" placeholder="输入昵称">
                </div>
                <div class="form-row">
                    <label class="form-label" for="dailyGoal">阅读目标</label>
                    <div class="goal-slider-wrap">
                        <input type="range" id="dailyGoal" name="dailyGoal" min="10" max="120" step="5"
                               value="<%=user.getDailyGoal() > 0 ? user.getDailyGoal() : 30%>" class="form-range">
                        <span class="goal-value" id="goalValue"><%=user.getDailyGoal() > 0 ? user.getDailyGoal() : 30%> 分钟</span>
                    </div>
                </div>
                <div class="form-actions"><button type="submit" class="btn-primary">保存</button></div>
            </form>
        </div>
    </div>

    <!-- ===== 个性化 ===== -->
    <div class="accordion-item">
        <div class="accordion-head" onclick="toggleAccordion(this)">
            <span class="accordion-title">个性化</span>
            <span class="accordion-arrow"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6,9 12,15 18,9"/></svg></span>
        </div>
        <div class="accordion-body">
            <form action="<%=ctx%>/settings" method="post" enctype="multipart/form-data" class="settings-form">
                <input type="hidden" name="action" value="update_profile">
                <input type="hidden" name="nickname" value="<%=user.getNickname() != null ? user.getNickname() : ""%>">
                <input type="hidden" name="dailyGoal" value="<%=user.getDailyGoal() > 0 ? user.getDailyGoal() : 30%>">
                <input type="hidden" name="removeMascot" value="0" id="removeMascotHidden">
                <input type="hidden" name="removeAvatar" value="0" id="removeAvatarHidden">

                <!-- 头像 -->
                <div class="form-row">
                    <label class="form-label">头像</label>
                    <div class="mascot-section">
                        <div class="avatar-preview" id="avatarPreview">
                            <% if (hasAvatar) { %>
                                <img src="<%=user.getAvatar()%>" alt="头像" id="avatarImg">
                            <% } else { %>
                                <div class="mascot-placeholder"><%=user.getNickname() != null ? user.getNickname().substring(0,1) : "U"%></div>
                            <% } %>
                        </div>
                        <div class="mascot-controls">
                            <label class="btn-file">
                                <input type="file" name="avatarFile" accept="image/*" id="avatarFile" style="display:none">
                                选择图片
                            </label>
                            <label class="btn-remove" id="removeAvatarLabel">移除头像</label>
                        </div>
                    </div>
                </div>

                <!-- 背景图（模仿 VSCode background 插件） -->
                <div class="form-row">
                    <label class="form-label">背景图</label>
                    <div class="mascot-section">
                        <div class="mascot-preview" id="mascotPreview">
                            <% if (hasMascot) { %>
                                <img src="<%=user.getMascotImage()%>" alt="背景" style="opacity: <%=user.getMascotOpacity() / 100.0%>" id="mascotImg">
                            <% } else { %>
                                <div class="mascot-placeholder">未设置</div>
                            <% } %>
                        </div>
                        <div class="mascot-controls">
                            <label class="btn-file">
                                <input type="file" name="mascotFile" accept="image/*" id="mascotFile" style="display:none">
                                选择图片
                            </label>
                            <label class="btn-remove" id="removeMascotLabel">移除背景</label>
                            <div class="opacity-row">
                                <span class="opacity-label">透明度</span>
                                <input type="range" name="mascotOpacity" id="mascotOpacity" min="10" max="100" step="5"
                                       value="<%=user.getMascotOpacity() > 0 ? user.getMascotOpacity() : 80%>" class="form-range-sm">
                                <span class="opacity-value" id="opacityValue"><%=user.getMascotOpacity() > 0 ? user.getMascotOpacity() : 80%>%</span>
                            </div>
                            <div class="opacity-row">
                                <span class="opacity-label">缩放</span>
                                <input type="range" name="bgScale" id="bgScale" min="20" max="300" step="10"
                                       value="<%=user.getBgScale() > 0 ? user.getBgScale() : 100%>" class="form-range-sm">
                                <span class="opacity-value" id="bgScaleValue"><%=user.getBgScale() > 0 ? user.getBgScale() : 100%>%</span>
                            </div>
                            <div class="opacity-row">
                                <span class="opacity-label">镜像</span>
                                <label class="switch-row">
                                    <input type="checkbox" name="bgMirror" value="1" id="bgMirror" <%=user.getBgMirror() == 1 ? "checked" : ""%>>
                                    <span class="switch-text">水平翻转</span>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-actions"><button type="submit" class="btn-primary">保存</button></div>
            </form>
        </div>
    </div>

    <!-- ===== 修改密码 ===== -->
    <div class="accordion-item">
        <div class="accordion-head" onclick="toggleAccordion(this)">
            <span class="accordion-title">修改密码</span>
            <span class="accordion-arrow"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6,9 12,15 18,9"/></svg></span>
        </div>
        <div class="accordion-body">
            <form action="<%=ctx%>/settings" method="post" enctype="multipart/form-data" class="settings-form">
                <input type="hidden" name="action" value="change_password">
                <div class="form-row">
                    <label class="form-label" for="oldPassword">当前密码</label>
                    <input type="password" id="oldPassword" name="oldPassword" class="form-input" required placeholder="输入当前密码">
                </div>
                <div class="form-row">
                    <label class="form-label" for="newPassword">新密码</label>
                    <input type="password" id="newPassword" name="newPassword" class="form-input" required minlength="6" placeholder="至少 6 位">
                </div>
                <div class="form-actions"><button type="submit" class="btn-primary">修改</button></div>
            </form>
        </div>
    </div>

    <!-- ===== 导入历史 ===== -->
    <div class="accordion-item">
        <div class="accordion-head" onclick="toggleAccordion(this)">
            <span class="accordion-title">导入历史</span>
            <span class="accordion-arrow"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6,9 12,15 18,9"/></svg></span>
        </div>
        <div class="accordion-body">
            <% if (importHistory.isEmpty()) { %>
                <div class="empty-mini">暂无导入记录</div>
            <% } else { %>
            <div class="import-list">
                <% for (Book b : importHistory) { %>
                <div class="import-item">
                    <div class="import-info">
                        <span class="import-title"><%=b.getTitle()%></span>
                        <span class="import-meta"><%=b.getChapterCount()%> 章 · <%=b.getTotalWords() >= 10000 ? String.format("%.1f", b.getTotalWords()/10000.0) + " 万字" : b.getTotalWords() + " 字"%></span>
                    </div>
                    <span class="import-date"><%=b.getCreateTime() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(b.getCreateTime()) : ""%></span>
                </div>
                <% } %>
            </div>
            <% } %>
        </div>
    </div>

    <!-- ===== 关于 ===== -->
    <div class="accordion-item">
        <div class="accordion-head" onclick="toggleAccordion(this)">
            <span class="accordion-title">关于</span>
            <span class="accordion-arrow"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6,9 12,15 18,9"/></svg></span>
        </div>
        <div class="accordion-body">
            <div class="about-section">
                <div class="about-logo">
                    <div class="about-logo-circle">墨</div>
                    <div class="about-info">
                        <div class="about-name">墨读 Moeread</div>
                        <div class="about-version">版本 1.0.0</div>
                    </div>
                </div>
                <p class="about-desc">一个简洁的本地小说阅读器，支持 TXT/ZIP 导入、章节自动识别、阅读统计和摸鱼模式。</p>
                <a class="about-github" href="https://github.com/E3lct/moeread-novel" target="_blank">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/></svg>
                    <span>github.com/E3lct/moeread-novel</span>
                </a>
                <div class="about-tech">
                    <span class="tech-tag">JSP</span><span class="tech-tag">Servlet</span><span class="tech-tag">MySQL</span><span class="tech-tag">Tomcat 9</span>
                </div>
            </div>
        </div>
    </div>

    <div class="logout-row"><a href="<%=ctx%>/logout" class="btn-logout">退出登录</a></div>

</div>
</div>

<!-- ===== 背景图预览（实际渲染在 navbar include 的 _background.jsp） ===== -->

<script>
function toggleAccordion(head) {
    var item = head.parentElement;
    var isOpen = item.classList.contains('open');
    var all = document.querySelectorAll('.accordion-item');
    for (var i = 0; i < all.length; i++) all[i].classList.remove('open');
    if (!isOpen) item.classList.add('open');
}

// 阅读目标滑块
var goalSlider = document.getElementById('dailyGoal');
var goalValue = document.getElementById('goalValue');
if (goalSlider && goalValue) {
    goalSlider.addEventListener('input', function() { goalValue.textContent = this.value + ' 分钟'; });
}

// 透明度滑块
var opacitySlider = document.getElementById('mascotOpacity');
var opacityValue = document.getElementById('opacityValue');
var mascotImg = document.getElementById('mascotImg');
var bgLayer = document.getElementById('bgLayer');
if (opacitySlider && opacityValue) {
    opacitySlider.addEventListener('input', function() {
        opacityValue.textContent = this.value + '%';
        if (mascotImg) mascotImg.style.opacity = this.value / 100;
        if (bgLayer) bgLayer.style.opacity = this.value / 100;
    });
}

// 缩放滑块
var bgScaleSlider = document.getElementById('bgScale');
var bgScaleValue = document.getElementById('bgScaleValue');
if (bgScaleSlider && bgScaleValue) {
    bgScaleSlider.addEventListener('input', function() {
        bgScaleValue.textContent = this.value + '%';
        if (bgLayer) bgLayer.style.backgroundSize = this.value + '% ' + this.value + '%';
    });
}

// 镜像开关
var bgMirror = document.getElementById('bgMirror');
if (bgMirror) {
    bgMirror.addEventListener('change', function() {
        if (bgLayer) bgLayer.style.transform = this.checked ? 'scaleX(-1)' : 'scaleX(1)';
    });
}

// 看板娘文件预览
var mascotFile = document.getElementById('mascotFile');
var mascotPreview = document.getElementById('mascotPreview');
if (mascotFile) {
    mascotFile.addEventListener('change', function(e) {
        var file = e.target.files[0];
        if (file && file.size < 2 * 1024 * 1024) {
            var reader = new FileReader();
            reader.onload = function(ev) {
                mascotPreview.innerHTML = '<img src="' + ev.target.result + '" alt="预览" id="mascotImg">';
                if (opacitySlider) document.getElementById('mascotImg').style.opacity = opacitySlider.value / 100;
            };
            reader.readAsDataURL(file);
        }
    });
}

// 移除看板娘
var removeMascotLabel = document.getElementById('removeMascotLabel');
if (removeMascotLabel) {
    var mascotRemoved = false;
    removeMascotLabel.addEventListener('click', function(e) {
        e.preventDefault();
        mascotRemoved = !mascotRemoved;
        document.getElementById('removeMascotHidden').value = mascotRemoved ? '1' : '0';
        this.classList.toggle('active', mascotRemoved);
        mascotPreview.style.opacity = mascotRemoved ? '0.3' : '1';
    });
}

// 头像文件预览
var avatarFile = document.getElementById('avatarFile');
var avatarPreview = document.getElementById('avatarPreview');
if (avatarFile) {
    avatarFile.addEventListener('change', function(e) {
        var file = e.target.files[0];
        if (file && file.size < 2 * 1024 * 1024) {
            var reader = new FileReader();
            reader.onload = function(ev) {
                avatarPreview.innerHTML = '<img src="' + ev.target.result + '" alt="头像" id="avatarImg">';
            };
            reader.readAsDataURL(file);
        }
    });
}

// 移除头像
var removeAvatarLabel = document.getElementById('removeAvatarLabel');
if (removeAvatarLabel) {
    var avatarRemoved = false;
    removeAvatarLabel.addEventListener('click', function(e) {
        e.preventDefault();
        avatarRemoved = !avatarRemoved;
        document.getElementById('removeAvatarHidden').value = avatarRemoved ? '1' : '0';
        this.classList.toggle('active', avatarRemoved);
        avatarPreview.style.opacity = avatarRemoved ? '0.3' : '1';
    });
}

// 提示自动消失
var alerts = document.querySelectorAll('.alert');
for (var i = 0; i < alerts.length; i++) {
    (function(a) {
        setTimeout(function() { a.style.opacity = '0'; a.style.transform = 'translateY(-8px)'; }, 2500);
        setTimeout(function() { a.remove(); }, 3000);
    })(alerts[i]);
}
</script>
</body>
</html>
