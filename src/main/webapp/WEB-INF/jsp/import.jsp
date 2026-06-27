<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>导入 - 墨读 Moeread</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/import.css">
</head>
<body>
    <div class="import-wrapper">
        <div class="import-header">
            <h1>导入书籍</h1>
            <p>支持 TXT 单文件导入和 ZIP 批量导入</p>
        </div>

        <%-- 成功提示 --%>
        <% if (request.getAttribute("success") != null) { %>
            <div class="import-message success">${success}</div>
        <% } %>

        <%-- 错误提示 --%>
        <% if (request.getAttribute("error") != null) { %>
            <div class="import-message error">${error}</div>
        <% } %>

        <%-- 说明卡片 --%>
        <div class="info-card">
            <h3>导入说明</h3>
            <ul>
                <li>TXT 文件：自动识别 UTF-8 / GBK 编码</li>
                <li>章节识别：支持「第X章」「第X回」「第X节」「Chapter X」等格式</li>
                <li>ZIP 文件：批量导入，每个 TXT 作为一本书</li>
                <li>单文件限制：50MB</li>
            </ul>
        </div>

        <%-- 上传表单 --%>
        <form action="${pageContext.request.contextPath}/import" method="post" enctype="multipart/form-data" id="importForm">
            <div class="upload-zone" id="uploadZone" onclick="document.getElementById('fileInput').click()">
                <div class="upload-icon">+</div>
                <div class="upload-text">点击选择文件或拖拽到此处</div>
                <div class="upload-hint">可选择多个文件</div>
                <div class="upload-formats">支持 .txt / .zip</div>
            </div>
            <input type="file" name="files" id="fileInput" class="upload-input"
                   multiple accept=".txt,.zip" onchange="handleFileSelect(this)">

            <%-- 文件列表 --%>
            <div class="file-list" id="fileList"></div>

            <div class="import-actions">
                <button type="submit" class="import-btn" id="submitBtn" disabled>开始导入</button>
            </div>
        </form>

        <div class="import-back">
            <a href="${pageContext.request.contextPath}/home">返回首页</a>
        </div>
    </div>

    <script>
        // 文件选择后显示列表
        function handleFileSelect(input) {
            var fileList = document.getElementById('fileList');
            fileList.innerHTML = '';
            var files = input.files;
            if (files.length === 0) {
                document.getElementById('submitBtn').disabled = true;
                return;
            }

            for (var i = 0; i < files.length; i++) {
                var f = files[i];
                var item = document.createElement('div');
                item.className = 'file-item';

                var sizeStr = formatSize(f.size);
                item.innerHTML = '<span class="file-name">' + f.name + '</span>'
                              + '<span class="file-size">' + sizeStr + '</span>';

                fileList.appendChild(item);
            }
            document.getElementById('submitBtn').disabled = false;
        }

        function formatSize(bytes) {
            if (bytes < 1024) return bytes + ' B';
            if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
            return (bytes / 1024 / 1024).toFixed(1) + ' MB';
        }

        // 拖拽上传支持
        var zone = document.getElementById('uploadZone');
        zone.addEventListener('dragover', function(e) {
            e.preventDefault();
            zone.classList.add('dragover');
        });
        zone.addEventListener('dragleave', function(e) {
            zone.classList.remove('dragover');
        });
        zone.addEventListener('drop', function(e) {
            e.preventDefault();
            zone.classList.remove('dragover');
            // 把拖进来的文件赋给 input
            var input = document.getElementById('fileInput');
            input.files = e.dataTransfer.files;
            handleFileSelect(input);
        });
    </script>
</body>
</html>
