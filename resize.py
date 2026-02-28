import sys
from PIL import Image

def resize_image(input_path, output_path, width, height):
    try:
        img = Image.open(input_path)
        img = img.resize((width, height), Image.Resampling.LANCZOS)
        img.save(output_path)
        print(f"Successfully resized {input_path} to {width}x{height} -> {output_path}")
    except Exception as e:
        print(f"Error resizing {input_path}: {e}")

if __name__ == "__main__":
    if len(sys.argv) != 5:
        print("Usage: python resize.py <input> <output> <width> <height>")
        sys.exit(1)
        
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    width = int(sys.argv[3])
    height = int(sys.argv[4])
    
    resize_image(input_file, output_file, width, height)
