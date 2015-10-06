/*
 *  Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */

package scouter.agent.asm;

import java.util.List;

import scouter.agent.ClassDesc;
import scouter.agent.Configure;
import scouter.agent.asm.util.AsmUtil;
import scouter.agent.asm.util.MethodSet;
import scouter.agent.trace.TraceMain;
import scouter.lang.pack.XLogTypes;
import scouter.org.objectweb.asm.ClassVisitor;
import scouter.org.objectweb.asm.Label;
import scouter.org.objectweb.asm.MethodVisitor;
import scouter.org.objectweb.asm.Opcodes;
import scouter.org.objectweb.asm.Type;
import scouter.org.objectweb.asm.commons.LocalVariablesSorter;

public class ServiceASM implements IASM, Opcodes {
	private List<MethodSet> target = MethodSet.getHookingMethodSet(Configure.getInstance().hook_service);

	public ServiceASM() {
		//MethodSet.add(target, "com/netflix/discovery/DiscoveryClient", "makeRemoteCall", XLogTypes.BACK_THREAD);
		//MethodSet.add(target, "com/netflix/eureka/PeerAwareInstanceRegistry", "syncUp", XLogTypes.BACK_THREAD);
	}


	public boolean isTarget(String className) {
		for (int i = 0; i < target.size(); i++) {
			MethodSet mset = target.get(i);
			if (mset.classMatch.include(className)) {
				return true;
			}
		}
		return false;
	}

	public ClassVisitor transform(ClassVisitor cv, String className, ClassDesc classDesc) {

		for (int i = 0; i < target.size(); i++) {
			MethodSet mset = target.get(i);
			if (mset.classMatch.include(className)) {
				return new ServiceCV(cv, mset, className, mset.xType == 0 ? XLogTypes.APP_SERVICE : mset.xType);
			}
		}
		return cv;

	}

}

class ServiceCV extends ClassVisitor implements Opcodes {

	public String className;
	private MethodSet mset;
	private byte xType;

	public ServiceCV(ClassVisitor cv, MethodSet mset, String className,byte xType) {
		super(ASM4, cv);
		this.mset = mset;
		this.className = className;
		this.xType=xType;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if (mv == null || mset.isA(name, desc) == false) {
			return mv;
		}
		if (AsmUtil.isSpecial(name)) {
			return mv;
		}

		String fullname = AsmUtil.add(className, name, desc);
		return new ServiceMV(access, desc, mv, fullname,Type.getArgumentTypes(desc),(access & ACC_STATIC) != 0,xType,className,
				name, desc);
	}
}

class ServiceMV extends LocalVariablesSorter implements Opcodes {
	private static final String TRACEMAIN = TraceMain.class.getName().replace('.', '/');
	private final static String START_METHOD = "startService";
	private static final String START_SIGNATURE = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/Object;B)Ljava/lang/Object;";
	private final static String END_METHOD = "endService";
	private static final String END_SIGNATURE = "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Throwable;)V";

	private Label startFinally = new Label();
	private byte xType;

	public ServiceMV(int access, String desc, MethodVisitor mv, String fullname,Type[] paramTypes,
			boolean isStatic,byte xType,String classname, String methodname, String methoddesc) {
		super(ASM4, access, desc, mv);
		this.fullname = fullname;
		this.paramTypes = paramTypes;
		this.strArgIdx = AsmUtil.getStringIdx(access, desc);
		this.xType=xType;
		this.isStatic = isStatic;
		this.returnType = Type.getReturnType(desc);
		this.className = classname;
		this.methodName = methodname;
		this.methodDesc = methoddesc;
	}

	private Type[] paramTypes;
	private Type returnType;
	private String fullname;
	private int statIdx;
	private int strArgIdx;
	private boolean isStatic;
	private String className;
	private String methodName;
	private String methodDesc;
	@Override
	public void visitCode() {
		int sidx = isStatic ? 0 : 1;
		if (strArgIdx >= 0) {
			mv.visitVarInsn(Opcodes.ALOAD, strArgIdx);
		} else {
			AsmUtil.PUSH(mv, fullname);
		}
		
		int arrVarIdx = newLocal(Type.getType("[Ljava/lang/Object;"));
		AsmUtil.PUSH(mv, paramTypes.length);
		mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
		mv.visitVarInsn(Opcodes.ASTORE, arrVarIdx);

		for (int i = 0; i < paramTypes.length; i++) {
			Type tp = paramTypes[i];
			mv.visitVarInsn(Opcodes.ALOAD, arrVarIdx);
			AsmUtil.PUSH(mv, i);

			switch (tp.getSort()) {
			case Type.BOOLEAN:
				mv.visitVarInsn(Opcodes.ILOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;",false);
				break;
			case Type.BYTE:
				mv.visitVarInsn(Opcodes.ILOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;",false);
				break;
			case Type.CHAR:
				mv.visitVarInsn(Opcodes.ILOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;",false);
				break;
			case Type.SHORT:
				mv.visitVarInsn(Opcodes.ILOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;",false);
				break;
			case Type.INT:
				mv.visitVarInsn(Opcodes.ILOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
				break;
			case Type.LONG:
				mv.visitVarInsn(Opcodes.LLOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;",false);
				break;
			case Type.FLOAT:
				mv.visitVarInsn(Opcodes.FLOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;",false);
				break;
			case Type.DOUBLE:
				mv.visitVarInsn(Opcodes.DLOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;",false);
				break;
			default:
				mv.visitVarInsn(Opcodes.ALOAD, sidx);
			}
			mv.visitInsn(Opcodes.AASTORE);
			sidx += tp.getSize();
		}
		
		AsmUtil.PUSH(mv, className);
		AsmUtil.PUSH(mv, methodName);
		AsmUtil.PUSH(mv, methodDesc);
		
		if (isStatic) {
			AsmUtil.PUSHNULL(mv);
		} else {
			mv.visitVarInsn(Opcodes.ALOAD, 0);
		}
		mv.visitVarInsn(Opcodes.ALOAD, arrVarIdx);
		
		
		AsmUtil.PUSH(mv, xType);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, TRACEMAIN, START_METHOD, START_SIGNATURE,false);

		statIdx = newLocal(Type.getType(Object.class));
		mv.visitVarInsn(Opcodes.ASTORE, statIdx);
		mv.visitLabel(startFinally);
		mv.visitCode();
	}

	@Override
	public void visitInsn(int opcode) {
		if ((opcode >= IRETURN && opcode <= RETURN)) {
//			mv.visitVarInsn(Opcodes.ALOAD, statIdx);
//			mv.visitInsn(Opcodes.ACONST_NULL);
//			mv.visitMethodInsn(Opcodes.INVOKESTATIC, TRACEMAIN, END_METHOD, END_SIGNATURE);
			capReturn();
		}
		mv.visitInsn(opcode);
	}
	private void capReturn() {
		Type tp = returnType;

		if (tp == null || tp.equals(Type.VOID_TYPE)) {

			mv.visitVarInsn(Opcodes.ALOAD, statIdx);
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, TRACEMAIN, END_METHOD, END_SIGNATURE, false);
			return;
		}
		int i = newLocal(tp);
		switch (tp.getSort()) {
		case Type.BOOLEAN:
			mv.visitVarInsn(Opcodes.ISTORE, i);
			mv.visitVarInsn(Opcodes.ILOAD, i);

			mv.visitVarInsn(Opcodes.ALOAD, statIdx);// stat
			mv.visitVarInsn(Opcodes.ILOAD, i);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
			mv.visitInsn(Opcodes.ACONST_NULL);// throwable
			break;
		case Type.BYTE:
			mv.visitVarInsn(Opcodes.ISTORE, i);
			mv.visitVarInsn(Opcodes.ILOAD, i);

			mv.visitVarInsn(Opcodes.ALOAD, statIdx);// stat
			mv.visitVarInsn(Opcodes.ILOAD, i);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
			mv.visitInsn(Opcodes.ACONST_NULL);// throwable
			break;
		case Type.CHAR:
			mv.visitVarInsn(Opcodes.ISTORE, i);
			mv.visitVarInsn(Opcodes.ILOAD, i);

			mv.visitVarInsn(Opcodes.ALOAD, statIdx);// stat
			mv.visitVarInsn(Opcodes.ILOAD, i);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;",
					false);
			mv.visitInsn(Opcodes.ACONST_NULL);// throwable
			break;
		case Type.SHORT:
			mv.visitVarInsn(Opcodes.ISTORE, i);
			mv.visitVarInsn(Opcodes.ILOAD, i);

			mv.visitVarInsn(Opcodes.ALOAD, statIdx);// stat
			mv.visitVarInsn(Opcodes.ILOAD, i);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
			mv.visitInsn(Opcodes.ACONST_NULL);// throwable
			break;
		case Type.INT:
			mv.visitVarInsn(Opcodes.ISTORE, i);
			mv.visitVarInsn(Opcodes.ILOAD, i);

			mv.visitVarInsn(Opcodes.ALOAD, statIdx);// stat
			mv.visitVarInsn(Opcodes.ILOAD, i);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			mv.visitInsn(Opcodes.ACONST_NULL);// throwable
			break;
		case Type.LONG:
			mv.visitVarInsn(Opcodes.LSTORE, i);
			mv.visitVarInsn(Opcodes.LLOAD, i);

			mv.visitVarInsn(Opcodes.ALOAD, statIdx);// stat
			mv.visitVarInsn(Opcodes.LLOAD, i);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
			mv.visitInsn(Opcodes.ACONST_NULL);// throwable
			break;
		case Type.FLOAT:
			mv.visitVarInsn(Opcodes.FSTORE, i);
			mv.visitVarInsn(Opcodes.FLOAD, i);

			mv.visitVarInsn(Opcodes.ALOAD, statIdx);// stat
			mv.visitVarInsn(Opcodes.FLOAD, i);

			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
			mv.visitInsn(Opcodes.ACONST_NULL);// throwable
			break;
		case Type.DOUBLE:
			mv.visitVarInsn(Opcodes.DSTORE, i);
			mv.visitVarInsn(Opcodes.DLOAD, i);

			mv.visitVarInsn(Opcodes.ALOAD, statIdx);// stat
			mv.visitVarInsn(Opcodes.DLOAD, i);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
			mv.visitInsn(Opcodes.ACONST_NULL);// throwable
			break;
		default:
			mv.visitVarInsn(Opcodes.ASTORE, i);
			mv.visitVarInsn(Opcodes.ALOAD, i);

			mv.visitVarInsn(Opcodes.ALOAD, statIdx);// stat
			mv.visitVarInsn(Opcodes.ALOAD, i);// return
			mv.visitInsn(Opcodes.ACONST_NULL);// throwable
		}

		mv.visitMethodInsn(Opcodes.INVOKESTATIC, TRACEMAIN, END_METHOD, END_SIGNATURE, false);
	}
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		Label endFinally = new Label();
		mv.visitTryCatchBlock(startFinally, endFinally, endFinally, null);
		mv.visitLabel(endFinally);
		mv.visitInsn(DUP);
		int errIdx = newLocal(Type.getType(Throwable.class));
		mv.visitVarInsn(Opcodes.ASTORE, errIdx);

		mv.visitVarInsn(Opcodes.ALOAD, statIdx);
		mv.visitInsn(Opcodes.ACONST_NULL);// return
		mv.visitVarInsn(Opcodes.ALOAD, errIdx);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, TRACEMAIN, END_METHOD, END_SIGNATURE,false);
		mv.visitInsn(ATHROW);
		mv.visitMaxs(maxStack + 8, maxLocals + 2);
	}
}