import gradio as gr
from transformers import pipeline, CLIPProcessor, CLIPModel
from PIL import Image
import torch
import chromadb
import numpy as np
import os
import uuid
from datetime import datetime

print("正在載入 AI 模型...（首次可能需要幾分鐘）")

breed_classifier = pipeline(
    "image-classification", 
    model="MaxPowerUnlimited/vit-base-oxford-iiit-pets",
    top_k=1
)

clip_model = CLIPModel.from_pretrained("openai/clip-vit-base-patch32")
clip_processor = CLIPProcessor.from_pretrained("openai/clip-vit-base-patch32")

breed_to_species = {
    # 貓類 (12種)
    "Abyssinian": "貓", "Bengal": "貓", "Birman": "貓", "Bombay": "貓",
    "British_Shorthair": "貓", "Egyptian_Mau": "貓", "Maine_Coon": "貓",
    "Persian": "貓", "Ragdoll": "貓", "Russian_Blue": "貓", "Siamese": "貓",
    "Sphynx": "貓",
    # 狗類 (25種)
    "American_Bulldog": "狗", "American_Pit_Bull_Terrier": "狗", "Basset_Hound": "狗",
    "Beagle": "狗", "Boxer": "狗", "Bulldog": "狗", "Chihuahua": "狗",
    "English_Cocker_Spaniel": "狗", "English_Setter": "狗", "German_Shorthaired": "狗",
    "Great_Pyrenees": "狗", "Havanese": "狗", "Japanese_Chin": "狗", "Keeshond": "狗",
    "Leonberger": "狗", "Miniature_Pinscher": "狗", "Newfoundland": "狗",
    "Pomeranian": "狗", "Pug": "狗", "Saint_Bernard": "狗", "Samoyed": "狗",
    "Scottish_Terrier": "狗", "Shiba_Inu": "狗", "Shih-Tzu": "狗",
    "Staffordshire_Bull_Terrier": "狗", "Wheaten_Terrier": "狗", "Yorkshire_Terrier": "狗"
}

# 初始化 ChromaDB 資料庫（本地持久化）
db_path = os.path.expanduser("~/Users/manheicheung/Downloads/AIPet/PetAI_Database") # path和database的名稱（可更改)
os.makedirs(db_path, exist_ok=True)
chroma_client = chromadb.PersistentClient(path=db_path)
collection = chroma_client.get_or_create_collection(name="pets")

print(" 模型與資料庫載入完成！")

def get_embedding(image: Image.Image) -> np.ndarray:
    """提取圖片特徵向量（CLIP）"""
    inputs = clip_processor(images=image, return_tensors="pt")
    with torch.no_grad():
        features = clip_model.get_image_features(**inputs)
    return features[0].cpu().numpy()

def analyze_and_match(image: Image.Image, threshold: float = 0.85):
    """核心功能：分析 + 資料庫比對"""
    if image is None:
        return " 請上傳圖片", None, None
    
    # 1. 品種辨識
    breed_result = breed_classifier(image)
    breed_label = breed_result[0]['label']
    confidence = round(breed_result[0]['score'] * 100, 2)
    
    # 2. 物種判斷
    species = breed_to_species.get(breed_label, "未知")
    
    features_note = f"• 品種：{breed_label}（信心度 {confidence}%）\n• 物種：{species}\n• 特徵：毛色/體型/臉型等可透過資料庫匹配更精準判斷"
    
    embedding = get_embedding(image)
    results = collection.query(
        query_embeddings=[embedding.tolist()],
        n_results=3,
        include=["metadatas", "distances"]
    )
    
    match_info = ""
    match_image = None
    
    if results['distances'][0] and results['distances'][0][0] < (1 - threshold):  # cosine similarity 轉換
        best_match = results['metadatas'][0][0]
        similarity = round((1 - results['distances'][0][0]) * 100, 2)
        
        match_info = f"""
        ✅ <span class="match">高度匹配！</span> 這張照片屬於資料庫中的「<strong>{best_match['name']}</strong>」<br>
        📊 相似度：{similarity}%<br>
        🐾 品種：{best_match['breed']}
        🆔 物種：{best_match['species']}
        """
    else:
        match_info = f'<span class="no-match">⚠️ 資料庫中未找到高度匹配的寵物（相似度低於 {threshold*100}%）</span><br>建議將此照片新增至資料庫。'
    
    analysis_text = f"""
    🐾 <strong>分析結果</strong>
    • 物種：{species}
    • 品種：{breed_label}（{confidence}%）
    • {age_note}
    • {features_note}
    """
    
    return analysis_text, match_info, image

def add_to_database(image: Image.Image, pet_name: str, known_breed: str = None):
    """新增寵物到資料庫"""
    if image is None or not pet_name:
        return " 請上傳圖片並填寫名稱"
    
    embedding = get_embedding(image)
    pet_id = str(uuid.uuid4())
    
    # 自動辨識品種（若未提供）
    if not known_breed or known_breed == "":
        breed_result = breed_classifier(image)
        known_breed = breed_result[0]['label']
    
    species = breed_to_species.get(known_breed, "未知")
    
    collection.add(
        embeddings=[embedding.tolist()],
        metadatas=[{
            "name": pet_name,
            "breed": known_breed,
            "species": species,
            "added_time": datetime.now().strftime("%Y-%m-%d %H:%M")
        }],
        ids=[pet_id]
    )
    
    return f" 已成功新增「{pet_name}」到資料庫！\n品種：{known_breed}　物種：{species}　年齡：{pet_age}歲"

# ==================== Gradio 介面 ====================
with gr.Blocks(title="寵物AI辨識工具", theme=gr.themes.Soft()) as demo:
    gr.Markdown("# 🐱🐶 寵物AI智慧辨識工具\n上傳照片即可自動分析並與資料庫匹配")
    
    with gr.Tab("🔍 分析與匹配"):
        with gr.Row():
            with gr.Column(scale=1):
                input_image = gr.Image(type="pil", label="📸 上傳寵物照片（貓或狗）", height=400)
                match_btn = gr.Button("🚀 開始辨識與比對", variant="primary", size="large")
            with gr.Column(scale=1):
                analysis_output = gr.Markdown(label=" 分析結果")
                match_output = gr.Markdown(label=" 資料庫匹配結果")
                result_image = gr.Image(label="上傳圖片預覽", height=300)
        
        match_btn.click(
            fn=analyze_and_match,
            inputs=[input_image],
            outputs=[analysis_output, match_output, result_image]
        )
    
    with gr.Tab(" 新增至資料庫"):
        gr.Markdown("### 將已知寵物加入資料庫，未來即可快速辨識")
        with gr.Row():
            with gr.Column():
                add_image = gr.Image(type="pil", label="📸 上傳已知寵物照片")
                add_name = gr.Textbox(label="寵物名稱", placeholder="例如：小花")
                add_breed = gr.Textbox(label="品種（選填，留空自動辨識）", placeholder="例如：British_Shorthair")
                add_btn = gr.Button(" 新增到資料庫", variant="primary")
            with gr.Column():
                add_status = gr.Markdown()
        
        add_btn.click(
            fn=add_to_database,
            inputs=[add_image, add_name, add_breed],
            outputs=[add_status]
        )
    
demo.launch(share=False, server_name="0.0.0.0", server_port=7860)